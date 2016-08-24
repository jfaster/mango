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

package org.jfaster.mango.binding;

import org.jfaster.mango.annotation.Rename;
import org.jfaster.mango.reflect.ParameterDescriptor;

import java.util.*;

/**
 * @author ash
 */
public class NameProvider {

    /**
     * 位置到重命名后的变量名的映射
     */
    private Map<Integer, String> namesMap = new HashMap<Integer, String>();

    /**
     * 重命名后的变量
     */
    private Set<String> names = new HashSet<String>();

    public NameProvider(List<ParameterDescriptor> pds) {
        for (ParameterDescriptor pd : pds) {
            Rename renameAnno = pd.getAnnotation(Rename.class);
            int position = pd.getPosition();
            String parameterName = renameAnno != null ?
                    renameAnno.value() : // 优先使用注解中的名字
                    pd.getName();
            namesMap.put(position, parameterName);
            names.add(parameterName);
        }
    }

    public String getParameterName(int position) {
        String name = namesMap.get(position);
        if (name == null) {
            throw new IllegalStateException("parameter name can not be found by position [" + position + "]");
        }
        return name;
    }

    public boolean isParameterName(String name) {
        return names.contains(name);
    }

}
