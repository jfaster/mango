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

/**
 * @author ash
 */
public class Account {

  private int id;
  private int balance;

  public Account() {
  }

  public Account(int id, int balance) {
    this.id = id;
    this.balance = balance;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Account other = (Account) obj;
    return Objects.equal(this.id, other.id)
        && Objects.equal(this.balance, other.balance);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).add("balance", balance).toString();
  }

  public void add(int num) {
    balance += num;
  }

  public void sub(int num) {
    balance -= num;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getBalance() {
    return balance;
  }

  public void setBalance(int balance) {
    this.balance = balance;
  }
}
