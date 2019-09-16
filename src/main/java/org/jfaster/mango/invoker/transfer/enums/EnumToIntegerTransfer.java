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

package org.jfaster.mango.invoker.transfer.enums;

import org.jfaster.mango.invoker.PropertyTransfer;
import org.jfaster.mango.util.local.DoubleCheckCache;
import org.jfaster.mango.util.local.LoadingCache;
import org.jfaster.mango.util.reflect.TypeToken;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.EnumSet;

/**
 * Enum <--> Integer
 *
 * @author ash
 */
public class EnumToIntegerTransfer implements PropertyTransfer<Enum, Integer> {

  private final static LoadingCache<Class, EnumSet> cache = new DoubleCheckCache<>
      (
          enumType -> EnumSet.allOf(enumType)
      );

  @Nullable
  @Override
  public Integer propertyToColumn(@Nullable Enum propertyValue) {
    return propertyValue == null ? null : propertyValue.ordinal();
  }

  @Nullable
  @Override
  public Enum columnToProperty(@Nullable Integer columnValue, Type actualPropertyType) {
    if (columnValue == null) {
      return null;
    }
    Class<?> rawType = TypeToken.of(actualPropertyType).getRawType();
    EnumSet<?> es = cache.get(rawType);
    for (Enum<?> e : es) {
      if (e.ordinal() == columnValue) {
        return e;
      }
    }
    throw new IllegalStateException("cant' trans Integer(" + columnValue + ") to " + actualPropertyType);
  }

  @Override
  public boolean isCheckType() {
    return false;
  }
}
