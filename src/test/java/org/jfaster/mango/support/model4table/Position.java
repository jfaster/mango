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

import com.google.common.base.Objects;

/**
 * @author ash
 */
public class Position {

  private int x;

  private int y;

  private int v;

  public Position() {
  }

  public Position(int x, int y, int v) {
    this.x = x;
    this.y = y;
    this.v = v;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Position other = (Position) obj;
    return Objects.equal(this.x, other.x)
        && Objects.equal(this.y, other.y)
        && Objects.equal(this.v, other.v);
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public int getV() {
    return v;
  }

  public void setV(int v) {
    this.v = v;
  }
}
