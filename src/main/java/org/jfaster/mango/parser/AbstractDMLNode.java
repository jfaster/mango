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

package org.jfaster.mango.parser;

import org.jfaster.mango.binding.InvocationContext;
import org.jfaster.mango.util.jdbc.SQLType;

/**
 * @author ash
 */
public abstract class AbstractDMLNode extends AbstractRenderableNode {

  private String value;

  public AbstractDMLNode(int i) {
    super(i);
  }

  public AbstractDMLNode(Parser p, int i) {
    super(p, i);
  }

  @Override
  public boolean render(InvocationContext context) {
    context.writeToSqlBuffer(value);
    return true;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return super.toString() + "[" + value + "]";
  }

  public abstract SQLType getSQLType();

}
