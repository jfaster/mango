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

package org.jfaster.mango.crud;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.apache.commons.lang.RandomStringUtils;
import org.jfaster.mango.annotation.Column;
import org.jfaster.mango.annotation.ID;
import org.jfaster.mango.annotation.Ignore;

/**
 * @author ash
 */
public class CrudOrder {

  @ID(autoGenerateId = false)
  private String id;

  @Column("uid")
  private int userId;

  private int price;

  @Ignore
  private String stat;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getPrice() {
    return price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public String getStat() {
    return stat;
  }

  public void setStat(String stat) {
    this.stat = stat;
  }

  public static CrudOrder createRandomCrudOrder(int userId) {
    CrudOrder co = new CrudOrder();
    String id = RandomStringUtils.randomNumeric(10);
    int price = 100;
    co.setId(id);
    co.setUserId(userId);
    co.setPrice(price);
    return co;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final CrudOrder other = (CrudOrder) obj;
    return Objects.equal(this.id, other.id)
        && Objects.equal(this.userId, other.userId)
        && Objects.equal(this.price, other.price);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, userId, price);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).add("userId", userId).add("price", price).toString();
  }

}
