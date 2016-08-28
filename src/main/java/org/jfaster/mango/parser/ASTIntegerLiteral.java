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
 * 整数字面值
 *
 * @author ash
 */
public class ASTIntegerLiteral extends AbstractExpression {

  private Number value;

  public ASTIntegerLiteral(int i) {
    super(i);
  }

  public ASTIntegerLiteral(Parser p, int i) {
    super(p, i);
  }

  public void init(String str) {
    try {
      value = new Integer(str);
    } catch (NumberFormatException E1) {
      value = new Long(str);
    }
  }

  @Override
  public boolean evaluate(InvocationContext context) {
    return true;
  }

  @Override
  public Object value(InvocationContext context) {
    return value;
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