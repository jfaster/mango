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

package org.jfaster.mango.invoker.function.enums;

import org.jfaster.mango.invoker.RuntimeSetterFunction;
import org.jfaster.mango.util.local.CacheLoader;
import org.jfaster.mango.util.local.DoubleCheckCache;
import org.jfaster.mango.util.local.LoadingCache;
import org.jfaster.mango.util.reflect.TypeToken;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.EnumSet;

/**
 * Integer --> Enum
 *
 * @author ash
 */
public class IntegerToEnumFunction implements RuntimeSetterFunction<Integer, Enum> {

  private final static LoadingCache<Class, EnumSet> cache = new DoubleCheckCache<Class, EnumSet>(
      new CacheLoader<Class, EnumSet>() {
        public EnumSet load(Class enumType) {
          return EnumSet.allOf(enumType);
        }
      });

  @Nullable
  @Override
  public Enum apply(@Nullable Integer input, Type runtimeOutputType) {
    if (input == null) {
      return null;
    }
    Class<?> rawType = TypeToken.of(runtimeOutputType).getRawType();
    EnumSet<?> es = cache.get(rawType);
    for (Enum<?> e : es) {
      if (e.ordinal() == input) {
        return e;
      }
    }
    throw new IllegalStateException("cant' trans Integer(" + input + ") to " + runtimeOutputType);
  }

}
