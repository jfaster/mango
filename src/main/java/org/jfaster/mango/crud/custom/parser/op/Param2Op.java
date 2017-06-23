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

package org.jfaster.mango.crud.custom.parser.op;

/**
 * @author ash
 */
public abstract class Param2Op extends AbstractOp {

  @Override
  public int paramCount() {
    return 2;
  }

  @Override
  public String render(String column, String[] params) {
    if (params == null) {
      throw new NullPointerException("params can't be null");
    }
    if (params.length != 2) {
      throw new IllegalArgumentException("length of params expected 2, but " + params.length);
    }
    return render(column, params[0], params[1]);
  }

  public abstract String render(String column, String param1, String param2);

}
