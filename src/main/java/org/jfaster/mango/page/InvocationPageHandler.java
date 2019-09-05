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

package org.jfaster.mango.page;

import org.jfaster.mango.binding.BoundSql;
import org.jfaster.mango.binding.InvocationContext;
import org.jfaster.mango.descriptor.ParameterDescriptor;

import java.util.List;

/**
 * @author ash
 */
public class InvocationPageHandler {

  private final PageHandler pageHandler;

  private final List<ParameterDescriptor> parameterDescriptors;

  public InvocationPageHandler(PageHandler pageHandler,
                               List<ParameterDescriptor> parameterDescriptors) {
    this.pageHandler = pageHandler;
    this.parameterDescriptors = parameterDescriptors;
  }

  public void handlePageAndSort(BoundSql boundSql, InvocationContext context) {
    List<Object> parameterValues = context.getParameterValues();
    Page page = null;
    Sort sort = null;
    for (int i = 0; i < parameterValues.size(); i++) {
      ParameterDescriptor pd = parameterDescriptors.get(i);
      if (Page.class.equals(pd.getRawType())) {
        Object val = parameterValues.get(i);
        if (val == null) {
          throw new IllegalArgumentException("Parameter page is null");
        }
        page = (Page) val;
      }
      if (Sort.class.equals(pd.getRawType())) {
        Object val = parameterValues.get(i);
        if (val == null) {
          throw new IllegalArgumentException("Parameter sort is null");
        }
        sort = (Sort) val;
      }
    }
    if (page != null & sort != null) { // Page和Sort不能同时存在
      throw new IllegalArgumentException("page and sort can't be used on a query");
    }
    if (page != null) {
      pageHandler.handlePage(boundSql, page);
    }
    if (sort != null) {
      pageHandler.handleSort(boundSql, sort);
    }
  }

  public void handleCount(BoundSql boundSql) {
    pageHandler.handleCount(boundSql);
  }

}
