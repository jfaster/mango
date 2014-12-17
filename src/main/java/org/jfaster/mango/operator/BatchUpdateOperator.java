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

import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.reflect.MethodDescriptor;
import org.jfaster.mango.util.Iterables;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author ash
 */
public class BatchUpdateOperator extends AbstractOperator {

    protected Transformer transformer;

    protected BatchUpdateOperator(ASTRootNode rootNode, MethodDescriptor md) {
        super(rootNode);
        transformer = TRANSFORMERS.get(md.getRawReturnType());
        if (transformer == null) {
            throw new RuntimeException(); // TODO
        }
    }

    @Override
    public Object execute(Object[] values) {
        Object firstValue = values[0];
        if (firstValue == null) {
            throw new NullPointerException("batchUpdate's parameter can't be null");
        }
        Iterables iterables = new Iterables(firstValue);
        if (iterables.isEmpty()) {
            throw new IllegalArgumentException("batchUpdate's parameter can't be empty");
        }

        Map<DataSource, Group> gorupMap = new HashMap<DataSource, Group>();
        int t = 0;
        for (Object obj : iterables) {
            InvocationContext context = invocationContextFactory.newInvocationContext(new Object[]{obj});
            group(context, gorupMap, t++);
        }
        int[] ints = executeDb(gorupMap, t);
        return transformer.transform(ints);
    }

    protected void group(InvocationContext context, Map<DataSource, Group> groupMap, int position) {
        context.setGlobalTable(tableGenerator.getTable(context));
        DataSource ds = dataSourceGenerator.getDataSource(context);
        Group group = groupMap.get(ds);
        if (group == null) {
            group = new Group();
            groupMap.put(ds, group);
        }

        rootNode.render(context);
        PreparedSql preparedSql = context.getPreparedSql();
        invocationInterceptorChain.intercept(preparedSql, context); // 拦截器

        String sql = preparedSql.getSql();
        Object[] args = preparedSql.getArgs().toArray();
        group.add(sql, args, position);
    }

    protected int[] executeDb(Map<DataSource, Group> groupMap, int batchNum) {
        int[] r = new int[batchNum];
        long now = System.nanoTime();
        int t = 0;
        try {
            for (Map.Entry<DataSource, Group> entry : groupMap.entrySet()) {
                DataSource ds = entry.getKey();
                List<String> sqls = entry.getValue().getSqls();
                List<Object[]> batchArgs = entry.getValue().getBatchArgs();
                List<Integer> positions = entry.getValue().getPositions();
                int[] ints = isUniqueSql(sqls) ?
                        jdbcOperations.batchUpdate(ds, sqls.get(0), batchArgs) :
                        jdbcOperations.batchUpdate(ds, sqls, batchArgs);
                for (int i = 0; i < ints.length; i++) {
                    r[positions.get(i)] = ints[i];
                }
                t++;
            }
        } finally {
            long cost = System.nanoTime() - now;
            if (t == groupMap.entrySet().size()) {
                statsCounter.recordExecuteSuccess(cost);
            } else {
                statsCounter.recordExecuteException(cost);
            }
        }
        return r;
    }

    protected boolean isUniqueSql(List<String> sqls) {
        String sql = sqls.get(0);
        boolean r = true;
        for (int i = 1; i < sqls.size(); i++) {
            if (!sql.equals(sqls.get(i))) {
                r = false;
                break;
            }
        }
        return r;
    }

    protected static class Group {
        private List<String> sqls = new LinkedList<String>();
        private List<Object[]> batchArgs = new LinkedList<Object[]>();
        private List<Integer> positions = new LinkedList<Integer>();

        public void add(String sql, Object[] args, int position) {
            sqls.add(sql);
            batchArgs.add(args);
            positions.add(position);
        }

        public List<String> getSqls() {
            return sqls;
        }

        public List<Object[]> getBatchArgs() {
            return batchArgs;
        }

        public List<Integer> getPositions() {
            return positions;
        }
    }

    private final static Map<Class, Transformer> TRANSFORMERS = new HashMap<Class, Transformer>();
    static {
        TRANSFORMERS.put(int[].class, IntArrayTransformer.INSTANCE);
        TRANSFORMERS.put(Void.class, VoidTransformer.INSTANCE);
        TRANSFORMERS.put(void.class, VoidTransformer.INSTANCE);
    }

    interface Transformer {
        Object transform(int[] s);
    }

    enum IntArrayTransformer implements Transformer {
        INSTANCE;

        @Override
        public Object transform(int[] s) {
            return s;
        }
    }

    enum VoidTransformer implements Transformer {
        INSTANCE;

        @Override
        public Object transform(int[] s) {
            return null;
        }
    }

}
