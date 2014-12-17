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

        Class<?> wrapRawType = Primitives.wrap(md.getRawReturnType());
        if (returnGeneratedId) {
            transformer = GENERATED_TRANSFORMERS.get(wrapRawType);
            if (transformer == null) {
                throw new RuntimeException(); // TODO
            }
            if (Integer.class.equals(wrapRawType)) {
                rawReturnType = int.class;
            } else if (Long.class.equals(wrapRawType)) {
                rawReturnType = long.class;
            } else {
                throw new IllegalStateException(); // TODO
            }
        } else {
            transformer = TRANSFORMERS.get(wrapRawType);
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

    private final static Map<Class, Transformer> TRANSFORMERS = new HashMap<Class, Transformer>() {
        {
            put(Integer.class, IntTransformer.INSTANCE);
            put(Long.class, LongTransformer.INSTANCE);
            put(Void.class, VoidTransformer.INSTANCE);
            put(Boolean.class, BooleanTransformer.INSTANCE);
        }
    };

    private final static Map<Class, Transformer> GENERATED_TRANSFORMERS = new HashMap<Class, Transformer>() {
        {
            put(Integer.class, IntTransformer.INSTANCE);
            put(Long.class, LongTransformer.INSTANCE);
        }
    };

    interface Transformer {
        Object transform(Number r);
    }

    enum IntTransformer implements Transformer {
        INSTANCE;

        @Override
        public Object transform(Number r) {
            return r.intValue();
        }
    }

    enum LongTransformer implements Transformer {
        INSTANCE;

        @Override
        public Object transform(Number r) {
            return r.longValue();
        }
    }

    enum VoidTransformer implements Transformer {
        INSTANCE;

        @Override
        public Object transform(Number r) {
            return null;
        }
    }

    enum BooleanTransformer implements Transformer {
        INSTANCE;

        @Override
        public Object transform(Number r) {
            return r.intValue() > 0 ? Boolean.TRUE : Boolean.FALSE;
        }
    }

}
