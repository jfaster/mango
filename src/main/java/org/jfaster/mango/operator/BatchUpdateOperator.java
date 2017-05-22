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

import org.jfaster.mango.binding.BoundSql;
import org.jfaster.mango.binding.InvocationContext;
import org.jfaster.mango.descriptor.MethodDescriptor;
import org.jfaster.mango.exception.DescriptionException;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.stat.InvocationStat;
import org.jfaster.mango.transaction.*;
import org.jfaster.mango.util.Iterables;
import org.jfaster.mango.util.ToStringHelper;

import javax.sql.DataSource;
import java.util.*;

/**
 * @author ash
 */
public class BatchUpdateOperator extends AbstractOperator {

  protected Transformer transformer;

  public BatchUpdateOperator(ASTRootNode rootNode, MethodDescriptor md, Config config) {
    super(rootNode, md, config);
    transformer = TRANSFORMERS.get(md.getReturnRawType());
    if (transformer == null) {
      String expected = ToStringHelper.toString(TRANSFORMERS.keySet());
      throw new DescriptionException("the return type of batch update " +
          "expected one of " + expected + " but " + md.getReturnRawType());
    }
  }

  @Override
  public Object execute(Object[] values, InvocationStat stat) {
    Iterables iterables = getIterables(values);
    if (iterables.isEmpty()) {
      return transformer.transform(new int[]{});
    }

    Map<DataSource, Group> gorupMap = new HashMap<DataSource, Group>();
    int t = 0;
    for (Object obj : iterables) {
      InvocationContext context = invocationContextFactory.newInvocationContext(new Object[]{obj});
      group(context, gorupMap, t++);
    }
    int[] ints = executeDb(gorupMap, t, stat);
    return transformer.transform(ints);
  }

  protected void group(InvocationContext context, Map<DataSource, Group> groupMap, int position) {
    context.setGlobalTable(tableGenerator.getTable(context));
    DataSource ds = dataSourceGenerator.getDataSource(context, daoClass);
    Group group = groupMap.get(ds);
    if (group == null) {
      group = new Group();
      groupMap.put(ds, group);
    }

    rootNode.render(context);
    BoundSql boundSql = context.getBoundSql();
    invocationInterceptorChain.intercept(boundSql, context, ds); // 拦截器

    group.add(boundSql, position);
  }

  protected Iterables getIterables(Object[] values) {
    Object firstValue = values[0];
    if (firstValue == null) {
      throw new NullPointerException("batchUpdate's parameter can't be null");
    }
    Iterables iterables = new Iterables(firstValue);
    return iterables;
  }

  protected int[] executeDb(Map<DataSource, Group> groupMap, int batchNum, InvocationStat stat) {
    int[] r = new int[batchNum];
    long now = System.nanoTime();
    int t = 0;
    try {
      for (Map.Entry<DataSource, Group> entry : groupMap.entrySet()) {
        DataSource ds = entry.getKey();
        List<BoundSql> boundSqls = entry.getValue().getBoundSqls();
        List<Integer> positions = entry.getValue().getPositions();
        int[] ints = config.isUseTransactionForBatchUpdate() ?
            useTransactionBatchUpdate(ds, boundSqls) :
            jdbcOperations.batchUpdate(ds, boundSqls);
        for (int i = 0; i < ints.length; i++) {
          r[positions.get(i)] = ints[i];
        }
        t++;
      }
    } finally {
      long cost = System.nanoTime() - now;
      if (t == groupMap.entrySet().size()) {
        stat.recordDatabaseExecuteSuccess(cost);
      } else {
        stat.recordDatabaseExecuteException(cost);
      }
    }
    return r;
  }

  private int[]  useTransactionBatchUpdate(DataSource ds, List<BoundSql> boundSqls) {
    int[] ints;
    Transaction transaction = TransactionFactory.newTransaction(ds);
    try {
      ints = jdbcOperations.batchUpdate(ds, boundSqls);
    } catch (RuntimeException e) {
      transaction.rollback();
      throw e;
    }
    transaction.commit();
    return ints;
  }

  protected static class Group {
    private List<BoundSql> boundSqls = new LinkedList<BoundSql>();
    private List<Integer> positions = new LinkedList<Integer>();

    public void add(BoundSql boundSql, int position) {
      boundSqls.add(boundSql);
      positions.add(position);
    }

    public List<BoundSql> getBoundSqls() {
      return boundSqls;
    }

    public List<Integer> getPositions() {
      return positions;
    }
  }

  private final static Map<Class, Transformer> TRANSFORMERS = new LinkedHashMap<Class, Transformer>();

  static {
    TRANSFORMERS.put(void.class, VoidTransformer.INSTANCE);
    TRANSFORMERS.put(int.class, IntegerTransformer.INSTANCE);
    TRANSFORMERS.put(int[].class, IntArrayTransformer.INSTANCE);
    TRANSFORMERS.put(Void.class, VoidTransformer.INSTANCE);
    TRANSFORMERS.put(Integer.class, IntegerTransformer.INSTANCE);
    TRANSFORMERS.put(Integer[].class, IntegerArrayTransformer.INSTANCE);
  }

  public interface Transformer {
    Object transform(int[] s);
  }

  enum IntArrayTransformer implements Transformer {
    INSTANCE;

    @Override
    public Object transform(int[] s) {
      return s;
    }
  }

  enum IntegerArrayTransformer implements Transformer {
    INSTANCE;

    @Override
    public Object transform(int[] s) {
      Integer[] r = new Integer[s.length];
      for (int i = 0; i < s.length; i++) {
        r[i] = s[i];
      }
      return r;
    }
  }

  enum IntegerTransformer implements Transformer {
    INSTANCE;

    @Override
    public Object transform(int[] s) {
      int r = 0;
      for (int e : s) {
        r += e;
      }
      return r;
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
