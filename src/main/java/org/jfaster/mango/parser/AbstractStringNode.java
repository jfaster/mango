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

/**
 * @author ash
 */
public abstract class AbstractStringNode extends AbstractRenderableNode {

  private String value;

  private String groupValue;

  public AbstractStringNode(int i) {
    super(i);
  }

  public AbstractStringNode(Parser p, int i) {
    super(p, i);
  }

  @Override
  public boolean render(InvocationContext context) {
    context.writeToSqlBuffer(groupValue);
    return true;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getGroupValue() {
    return groupValue;
  }

  public void setGroupValue(String groupValue) {
    this.groupValue = groupValue;
  }

  @Override
  public String toString() {
    return super.toString() + "[" + value + "]";
  }

  @Override
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

}
