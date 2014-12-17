/*
 * Copyright 2014 mango.jfaster.org
 *
 * The Mango Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jfaster.mango.operator;

import org.jfaster.mango.annotation.ReturnGeneratedId;
import org.jfaster.mango.jdbc.GeneratedKeyHolder;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.reflect.MethodDescriptor;
import org.jfaster.mango.reflect.Primitives;
import org.jfaster.mango.util.SQLType;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ash
 */
public class UpdateOperator extends AbstractOperator {

    private boolean returnGeneratedId;

    private Transformer transformer;

    private Class<? extends Number> rawReturnType;

    protected UpdateOperator(ASTRootNode rootNode, MethodDescriptor md) {
        super(rootNode);
        init(md, rootNode.getSQLType());
    }

    private void init(MethodDescriptor md, SQLType sqlType) {
        ReturnGeneratedId returnGeneratedIdAnno = md.getAnnotation(ReturnGeneratedId.class);
        returnGeneratedId = returnGeneratedIdAnno != null // 要求返回自增id
                && sqlType == SQLType.INSERT; // 是插入语句

        Class<?> rawType = md.getRawReturnType();
        if (returnGeneratedId) {
            transformer = GENERATED_TRANSFORMERS.get(rawType);
            if (transformer == null) {
                throw new RuntimeException(); // TODO
            }
            if (Integer.class.equals(Primitives.wrap(rawType))) {
                rawReturnType = int.class;
            } else if (Long.class.equals(Primitives.wrap(rawType))) {
                rawReturnType = long.class;
            } else {
                throw new IllegalStateException(); // TODO
            }
        } else {
            transformer = TRANSFORMERS.get(rawType);
            if (transformer == null) {
                throw new RuntimeException(); // TODO
            }
        }
    }

    @Override
    public Object execute(Object[] values) {
        InvocationContext context = invocationContextFactory.newInvocationContext(values);
        return execute(context);
    }

    public Object execute(InvocationContext context) {
        context.setGlobalTable(tableGenerator.getTable(context));
        DataSource ds = dataSourceGenerator.getDataSource(context);

        rootNode.render(context);
        PreparedSql preparedSql = context.getPreparedSql();
        invocationInterceptorChain.intercept(preparedSql, context);  // 拦截器

        String sql = preparedSql.getSql();
        Object[] args = preparedSql.getArgs().toArray();
        Number r = executeDb(ds, sql, args);
        return transformer.transform(r);
    }

    private Number executeDb(DataSource ds, String sql, Object[] args) {
        Number r = null;
        long now = System.nanoTime();
        try {
            if (returnGeneratedId) {
                GeneratedKeyHolder holder = new GeneratedKeyHolder(rawReturnType);
                jdbcOperations.update(ds, sql, args, holder);
                r = holder.getKey();
            } else {
                r = jdbcOperations.update(ds, sql, args);
            }
        } finally {
            long cost = System.nanoTime() - now;
            if (r != null) {
                statsCounter.recordExecuteSuccess(cost);
            } else {
                statsCounter.recordExecuteException(cost);
            }
        }
        return r;
    }

    private final static Map<Class, Transformer> TRANSFORMERS = new HashMap<Class, Transformer>();
    static {
        TRANSFORMERS.put(Integer.class, IntegerTransformer.INSTANCE);
        TRANSFORMERS.put(int.class, IntegerTransformer.INSTANCE);
        TRANSFORMERS.put(Long.class, LongTransformer.INSTANCE);
        TRANSFORMERS.put(long.class, LongTransformer.INSTANCE);
        TRANSFORMERS.put(Void.class, VoidTransformer.INSTANCE);
        TRANSFORMERS.put(void.class, VoidTransformer.INSTANCE);
        TRANSFORMERS.put(Boolean.class, BooleanTransformer.INSTANCE);
        TRANSFORMERS.put(boolean.class, BooleanTransformer.INSTANCE);
    }

    private final static Map<Class, Transformer> GENERATED_TRANSFORMERS = new HashMap<Class, Transformer>();
    static {
        GENERATED_TRANSFORMERS.put(Integer.class, IntegerTransformer.INSTANCE);
        GENERATED_TRANSFORMERS.put(int.class, IntegerTransformer.INSTANCE);
        GENERATED_TRANSFORMERS.put(Long.class, LongTransformer.INSTANCE);
        GENERATED_TRANSFORMERS.put(long.class, LongTransformer.INSTANCE);
    }


    interface Transformer {
        Object transform(Number n);
    }

    enum IntegerTransformer implements Transformer {
        INSTANCE;

        @Override
        public Object transform(Number n) {
            return n.intValue();
        }
    }

    enum LongTransformer implements Transformer {
        INSTANCE;

        @Override
        public Object transform(Number n) {
            return n.longValue();
        }
    }

    enum VoidTransformer implements Transformer {
        INSTANCE;

        @Override
        public Object transform(Number n) {
            return null;
        }
    }

    enum BooleanTransformer implements Transformer {
        INSTANCE;

        @Override
        public Object transform(Number n) {
            return n.intValue() > 0 ? Boolean.TRUE : Boolean.FALSE;
        }
    }

}
