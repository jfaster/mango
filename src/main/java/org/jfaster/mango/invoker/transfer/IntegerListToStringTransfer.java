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

package org.jfaster.mango.invoker.transfer;

import org.jfaster.mango.invoker.PropertyTransfer;
import org.jfaster.mango.util.Joiner;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * List<Integer> <--> String
 *
 * @author ash
 */
public class IntegerListToStringTransfer implements PropertyTransfer<List<Integer>, String> {

  private final static String SEPARATOR = ",";

  @Nullable
  @Override
  public String propertyToColumn(@Nullable List<Integer> propValue) {
    if (propValue == null) {
      return null;
    }
    if (propValue.size() == 0) {
      return "";
    }
    return Joiner.on(SEPARATOR).join(propValue);
  }

  @Nullable
  @Override
  public List<Integer> columnToProperty(@Nullable String colValue, Type actualPropertyType) {
    if (colValue == null) {
      return null;
    }
    if (colValue.length() == 0) {
      return new ArrayList<>();
    }
    return Arrays.stream(colValue.split(SEPARATOR))
        .map(Integer::parseInt)
        .collect(Collectors.toList());
  }

}
