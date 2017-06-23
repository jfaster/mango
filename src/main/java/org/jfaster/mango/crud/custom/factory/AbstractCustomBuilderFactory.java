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

package org.jfaster.mango.crud.custom.factory;

import org.jfaster.mango.crud.Builder;
import org.jfaster.mango.crud.BuilderFactory;
import org.jfaster.mango.crud.CrudMeta;
import org.jfaster.mango.crud.common.builder.AbstractCommonBuilder;
import org.jfaster.mango.util.Strings;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ash
 */
public abstract class AbstractCustomBuilderFactory extends BuilderFactory {

  @Nullable
  @Override
  public Builder doTryGetBuilder(String name, Type returnType, List<Type> parameterTypes, Class<?> entityClass, Class<?> idClass) {
    int matchSize = metchSize(name);
    if (matchSize == 0) {
      return null;
    }
    String str = name.substring(matchSize);
    return null;
  }

  public abstract List<String> prefixs();

  abstract AbstractCommonBuilder createCustomBuilder(CrudMeta cm);

  private int metchSize(String name) {
    for (String prefix : prefixs()) {
      if (Strings.isEmpty(prefix)) {
        throw new IllegalStateException("prefix can't be empty");
      }
      Pattern p = Pattern.compile(prefix + "[A-Z]");
      Matcher m = p.matcher(name);
      if (m.find() && m.start() == 0) {
        return prefix.length();
      }
    }
    return 0;
  }

}
