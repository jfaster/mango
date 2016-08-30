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

package org.jfaster.mango.support.model4table;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Date;

/**
 * @author ash
 */
public class User {

  private int id;
  private String name;
  private int age;
  private boolean gender;
  private Long money;
  private Date updateTime;

  public User() {
  }

  public User(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public User(String name, int age, boolean gender, long money, Date updateTime) {
    this.name = name;
    this.age = age;
    this.gender = gender;
    this.money = money;
    this.updateTime = updateTime != null ? new Date(updateTime.getTime() / 1000 * 1000) : null; // 精确到秒
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final User other = (User) obj;
    Long thisUpdateTime = this.updateTime != null ? this.updateTime.getTime() : null;
    Long otherUpdateTime = other.updateTime != null ? other.updateTime.getTime() : null;
    return Objects.equal(this.id, other.id)
        && Objects.equal(this.name, other.name)
        && Objects.equal(this.age, other.age)
        && Objects.equal(this.gender, other.gender)
        && Objects.equal(this.money, other.money)
        && Objects.equal(thisUpdateTime, otherUpdateTime);
  }

  @Override
  public String toString() {
    Long thisUpdateTime = this.updateTime != null ? this.updateTime.getTime() : null;
    return MoreObjects.toStringHelper(this).add("id", id).add("name", name).add("age", age).
        add("gender", gender).add("money", money).add("updateTime", thisUpdateTime).toString();
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public boolean isGender() {
    return gender;
  }

  public void setGender(boolean gender) {
    this.gender = gender;
  }

  public Long getMoney() {
    return money;
  }

  public void setMoney(Long money) {
    this.money = money;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }
}
