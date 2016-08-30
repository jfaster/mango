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

import org.jfaster.mango.annotation.Rename;

import java.lang.annotation.Annotation;

/**
 * @author ash
 */
public class MockRename implements Annotation, Rename {

  private String value;

  public MockRename(String value) {
    this.value = value;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    throw new UnsupportedOperationException();
  }

}
