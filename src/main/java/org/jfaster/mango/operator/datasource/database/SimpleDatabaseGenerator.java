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

package org.jfaster.mango.operator.datasource.database;

import org.jfaster.mango.binding.InvocationContext;

/**
 * 简单database生成器，利用从{@link org.jfaster.mango.annotation.DB#database()}取得的database名称
 *
 * @author ash
 */
public class SimpleDatabaseGenerator implements DatabaseGenerator {

  private final String database;

  public SimpleDatabaseGenerator(String database) {
    this.database = database;
  }

  @Override
  public String getDatabase(InvocationContext context) {
    return database;
  }

}
