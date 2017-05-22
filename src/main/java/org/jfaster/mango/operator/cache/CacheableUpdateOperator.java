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

package org.jfaster.mango.operator.cache;

import org.jfaster.mango.binding.InvocationContext;
import org.jfaster.mango.descriptor.MethodDescriptor;
import org.jfaster.mango.exception.DescriptionException;
import org.jfaster.mango.operator.Config;
import org.jfaster.mango.operator.UpdateOperator;
import org.jfaster.mango.parser.ASTJDBCIterableParameter;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.stat.InvocationStat;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * @author ash
 */
public class CacheableUpdateOperator extends UpdateOperator {

  private final static InternalLogger logger = InternalLoggerFactory.getInstance(CacheableUpdateOperator.class);

  private CacheDriver driver;

  public CacheableUpdateOperator(ASTRootNode rootNode, MethodDescriptor md, CacheDriver cacheDriver, Config config) {
    super(rootNode, md, config);

    this.driver = cacheDriver;

    List<ASTJDBCIterableParameter> jips = rootNode.getJDBCIterableParameters();
    if (jips.size() > 1) {
      throw new DescriptionException("if use cache, sql's in clause expected less than or equal 1 but " +
          jips.size()); // sql中不能有多个in语句
    }
  }

  @Override
  public Object execute(Object[] values, InvocationStat stat) {
    InvocationContext context = invocationContextFactory.newInvocationContext(values);
    Object r = execute(context, stat);
    if (driver.isUseMultipleKeys()) { // 多个key，例如：update table set name='ash' where id in (1, 2, 3);
      Set<String> keys = driver.getCacheKeys(context);
      if (!keys.isEmpty()) {
        if (logger.isDebugEnabled()) {
          logger.debug("Cache delete for multiple keys {}", keys);
        }
        driver.batchDeleteFromCache(keys, stat);
      }
    } else { // 单个key，例如：update table set name='ash' where id ＝ 1;
      String key = driver.getCacheKey(context);
      if (logger.isDebugEnabled()) {
        logger.debug("Cache delete for single key [{}]", key);
      }
      driver.deleteFromCache(key, stat);
    }
    return r;
  }

}
