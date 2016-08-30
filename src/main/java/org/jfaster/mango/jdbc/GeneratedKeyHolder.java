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

package org.jfaster.mango.jdbc;

/**
 * @author ash
 */
public class GeneratedKeyHolder {

  private Number key;

  private Class<? extends Number> keyClass;

  public GeneratedKeyHolder(Class<? extends Number> keyClass) {
    this.keyClass = keyClass;
  }

  public Number getKey() {
    return key;
  }

  public void setKey(Object key) {
    this.key = Number.class.cast(key);
  }

  public Class<? extends Number> getKeyClass() {
    return keyClass;
  }
}
