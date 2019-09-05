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

import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class PageResult<T> {

  private final List<T> data;

  private final long total;

  public PageResult(List<T> data, long total) {
    this.data = data;
    this.total = total;
  }

  public List<T> getData() {
    return data;
  }

  public long getTotal() {
    return total;
  }

  public static <U> PageResult<U> empty(Class<U> clazz) {
    return new PageResult<>(new ArrayList<U>(), 0);
  }

}
