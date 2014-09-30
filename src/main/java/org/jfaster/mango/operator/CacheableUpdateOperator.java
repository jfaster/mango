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

import org.jfaster.mango.exception.IncorrectSqlException;
import org.jfaster.mango.parser.ASTJDBCIterableParameter;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.support.RuntimeContext;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * @author ash
 */
public class CacheableUpdateOperator extends UpdateOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(CacheableUpdateOperator.class);

    private CacheableOperatorDriver driver;

    protected CacheableUpdateOperator(ASTRootNode rootNode, CacheableOperatorDriver driver, Method method) {
        super(rootNode, driver, method);

        this.driver = driver;

        List<ASTJDBCIterableParameter> jips = rootNode.getJDBCIterableParameters();
        if (jips.size() > 1) {
            throw new IncorrectSqlException("if use cache, sql's in clause expected less than or equal 1 but " +
                    jips.size()); // sql中不能有多个in语句
        }
    }

    @Override
    public Object execute(Object[] values) {
        RuntimeContext context = driver.buildRuntimeContext(values);
        if (driver.isUseMultipleKeys()) { // 多个key，例如：update table set name='ash' where id in (1, 2, 3);
            Set<String> keys = driver.getCacheKeys(context);
            if (logger.isDebugEnabled()) {
                logger.debug("cache delete #keys={}", keys);
            }
            driver.deleteFromCache(keys);
        } else { // 单个key，例如：update table set name='ash' where id ＝ 1;
            String key = driver.getCacheKey(context);
            if (logger.isDebugEnabled()) {
                logger.debug("cache delete #key={}", key);
            }
            driver.deleteFromCache(key);
        }
        return execute(context);
    }

}
