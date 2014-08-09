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

package org.jfaster.mango.runtime.parser;

import org.jfaster.mango.runtime.RuntimeContext;
import org.jfaster.mango.runtime.TypeContext;

/**
 * @author ash
 */
public abstract class ValuableNode extends AbstractNode {

    public ValuableNode(int i) {
        super(i);
    }

    public ValuableNode(Parser p, int i) {
        super(p, i);
    }

    /**
     * 节点值
     *
     * @param context
     * @return
     */
    abstract Object value(RuntimeContext context);

    /**
     * 检测节点类型是否合法
     *
     * @param context
     */
    abstract void checkType(TypeContext context);

    /**
     * 获得语法块最开始的token
     *
     * @return
     */
    abstract Token getFirstToken();

    /**
     * 获得语法块最末位的token
     *
     * @return
     */
    abstract Token getLastToken();

    /**
     * 语法块字符串
     *
     * @return
     */
    protected String literal() {
        Token first = getFirstToken();
        Token last = getLastToken();

        if (first == last) {
            return first.image;
        }

        Token t = first;
        StringBuffer sb = new StringBuffer(t.image);
        while (t != last) {
            t = t.next;
            sb.append(t.image);
        }
        return sb.toString();
    }

}
