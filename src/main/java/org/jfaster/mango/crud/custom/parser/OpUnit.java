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

package org.jfaster.mango.crud.custom.parser;

import org.jfaster.mango.crud.custom.parser.op.*;
import org.jfaster.mango.util.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class OpUnit {

  private final static List<Op> OPS = new ArrayList<Op>();
  static {
    OPS.add(new IsNullOp());
    OPS.add(new NotNullOp());
    OPS.add(new TrueOp());
    OPS.add(new FalseOp());

    OPS.add(new LessThanOp());
    OPS.add(new LessThanEqualOp());
    OPS.add(new GreaterThanOp());
    OPS.add(new GreaterThanEqualOp());
    OPS.add(new NotOp());
    OPS.add(new InOp());
    OPS.add(new NotInOp());

    OPS.add(new BetweenOp());
  }

  private final Op op;

  private final String property;

  private OpUnit(String str) {
    for (Op op : OPS) {
      if (str.endsWith(op.keyword())) {
        this.op = op;
        this.property = Strings.firstLetterToLowerCase(str.substring(0, str.length() - op.keyword().length()));
        return;
      }
    }
    this.op = new EqualsOp();
    this.property = Strings.firstLetterToLowerCase(str);
  }

  public static OpUnit create(String str) {
    return new OpUnit(str);
  }

  public Op getOp() {
    return op;
  }

  public String getProperty() {
    return property;
  }

}
