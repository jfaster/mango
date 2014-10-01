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

import org.jfaster.mango.cache.CacheHandler;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.support.RuntimeContext;
import org.jfaster.mango.util.Iterables;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author ash
 */
public class CacheableBatchUpdateOperator extends BatchUpdateOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(CacheableBatchUpdateOperator.class);

    private CacheDriver driver;

    protected CacheableBatchUpdateOperator(ASTRootNode rootNode, Method method, CacheHandler cacheHandler) {
        super(rootNode, method);
        this.driver = new CacheDriverImpl(method, rootNode, cacheHandler, getTypeContext(), getNameProvider());
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

        Set<String> keys = new HashSet<String>(iterables.size() * 2);

        Map<DataSource, Group> groupMap = new HashMap<DataSource, Group>();
        for (Object obj : iterables) {
            RuntimeContext context = buildRuntimeContext(new Object[]{obj});
            keys.add(driver.getCacheKey(context));
            group(context, groupMap);
        }
        int[] ints = executeDb(groupMap);
        if (logger.isDebugEnabled()) {
            logger.debug("cache delete #keys={}", keys);
        }
        driver.deleteFromCache(keys);
        return ints;
    }

}
