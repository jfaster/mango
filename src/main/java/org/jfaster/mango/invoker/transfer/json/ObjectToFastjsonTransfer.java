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

package org.jfaster.mango.invoker.transfer.json;

import com.alibaba.fastjson.JSON;
import org.jfaster.mango.invoker.PropertyTransfer;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Object <--> String(fastjson)
 *
 * @author ash
 */
public class ObjectToFastjsonTransfer implements PropertyTransfer<Object, String> {

  @Override
  public String propertyToColumn(@Nullable Object propertyValue) {
    return propertyValue == null ? null : JSON.toJSONString(propertyValue);
  }

  @Nullable
  @Override
  public Object columnToProperty(@Nullable String columnValue, Type actualPropertyType) {
    return columnValue == null ? null : JSON.parseObject(columnValue, actualPropertyType);
  }

  @Override
  public boolean isCheckType() {
    return false;
  }
}
