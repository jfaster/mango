/*
 * Copyright 2014 mango.concurrent.cc
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

package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;

/**
 * @author ash
 */
public class ASTExpression extends ValuableExpression {

    public ASTExpression(int i) {
        super(i);
    }

    public ASTExpression(Parser p, int i) {
        super(p, i);
    }

    @Override
    Object value(RuntimeContext context) {
        return ((ValuableExpression) jjtGetChild(0)).value(context);
    }

    @Override
    void checkType(TypeContext context) {
        ((ValuableExpression) jjtGetChild(0)).checkType(context);
    }

    @Override
    Token getFirstToken() {
        return jjtGetFirstToken();
    }

    @Override
    Token getLastToken() {
        return jjtGetLastToken();
    }

}
