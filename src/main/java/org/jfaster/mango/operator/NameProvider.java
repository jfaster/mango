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

import org.jfaster.mango.annotation.Rename;
import org.jfaster.mango.reflect.ParameterDescriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ash
 */
public class NameProvider {

    /**
     * 重命名后的变量名
     */
    private Map<Integer, String> names = new HashMap<Integer, String>();

    public NameProvider(List<ParameterDescriptor> pds) {
        for (ParameterDescriptor pd : pds) {
            Rename renameAnno = pd.getAnnotation(Rename.class);
            int position = pd.getPosition();
            if (renameAnno != null) { // 优先使用注解中的名字
                names.put(position, renameAnno.value());
            } else {
                names.put(position, pd.getName());
            }
        }
    }

    public String getParameterName(int position) {
        String name = names.get(position);
        if (name == null) {
            throw new IllegalStateException("parameter name can not be found by position [" + position + "]");
        }
        return name;
    }

}
