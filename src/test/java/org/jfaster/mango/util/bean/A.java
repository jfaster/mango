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

package org.jfaster.mango.util.bean;

import org.jfaster.mango.annotation.Column;
import org.jfaster.mango.annotation.Getter;
import org.jfaster.mango.annotation.ID;
import org.jfaster.mango.annotation.Setter;
import org.jfaster.mango.invoker.function.IntArrayToStringFunction;
import org.jfaster.mango.invoker.function.StringToIntArrayFunction;

import javax.annotation.Nullable;

/**
 * @author ash
 */
public class A {

  @ID
  private int id;

  @Column("user_id")
  @Nullable
  private int uid;

  private int age;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Getter(IntArrayToStringFunction.class)
  public int getUid() {
    return uid;
  }

  @Setter(StringToIntArrayFunction.class)
  public void setUid(int uid) {
    this.uid = uid;
  }

  public String getName() {
    return "";
  }

  public void setName(String name) {
  }

  public int getAge() {
    return age;
  }

}
