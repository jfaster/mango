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

import org.jfaster.mango.support.SqlDescriptor;

import java.util.LinkedList;
import java.util.List;

/**
 * @author ash
 */
public class InterceptorChain {

    private List<Interceptor> interceptors;

    public void addInterceptor(Interceptor interceptor) {
        initInterceptorList();
        interceptors.add(interceptor);
    }

    public void intercept(SqlDescriptor sqlDescriptor, List<MethodParameter> methodParameters) {
        if (getInterceptors() != null) {
            for (Interceptor interceptor : getInterceptors()) {
                interceptor.intercept(sqlDescriptor, methodParameters);
            }
        }
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    private void initInterceptorList() {
        if (interceptors == null) {
            interceptors = new LinkedList<Interceptor>();
        }
    }

}
