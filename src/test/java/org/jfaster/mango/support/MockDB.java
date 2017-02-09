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

package org.jfaster.mango.support;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.datasource.AbstractDataSourceFactory;

import java.lang.annotation.Annotation;

/**
 * @author ash
 */
public class MockDB implements Annotation, DB {

  private String name = AbstractDataSourceFactory.DEFULT_NAME;

  private String table = "";

  public MockDB() {
  }

  public MockDB(String name, String table) {
    this.name = name;
    this.table = table;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String table() {
    return table;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    throw new UnsupportedOperationException();
  }

}
