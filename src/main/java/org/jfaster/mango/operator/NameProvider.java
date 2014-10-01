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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author ash
 */
public class NameProvider {

    /**
     * 重命名后的变量名
     */
    private String[] names;

    public NameProvider(Method method) {
        Annotation[][] pass = method.getParameterAnnotations();
        names = new String[pass.length];
        for (int i = 0; i < pass.length; i++) {
            Annotation[] pas = pass[i];
            for (Annotation pa : pas) {
                if (Rename.class.equals(pa.annotationType())) {
                    names[i] = ((Rename) pa).value();
                }
            }
        }
    }

    public String getParameterNameByIndex(int index) {
        String alias = names[index];
        return alias != null ? alias : String.valueOf(index + 1);
    }

}
