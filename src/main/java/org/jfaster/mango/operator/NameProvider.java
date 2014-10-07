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

package org.jfaster.mango.operator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ash
 */
public class NameProvider {

    /**
     * 重命名后的变量名
     */
    private Map<Integer, String> names = new HashMap<Integer, String>();

    public void setParameterName(int index, String name) {
        names.put(index, name);
    }

    public String getParameterName(int index) {
        String name = names.get(index);
        return name != null ? name : String.valueOf(index + 1);
    }

}
