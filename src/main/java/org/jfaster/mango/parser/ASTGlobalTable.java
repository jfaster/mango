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
public class ASTGlobalTable extends AbstractRenderableNode {

  public ASTGlobalTable(int i) {
    super(i);
  }

  public ASTGlobalTable(Parser p, int i) {
    super(p, i);
  }

  @Override
  public boolean render(InvocationContext context) {
    String table = context.getGlobalTable();
    if (table == null) {
      throw new IllegalStateException("global table in InvocationContext can't be null");
    }
    context.writeToSqlBuffer(table);
    return true;
  }

  @Override
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

}
