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

package org.jfaster.mango.page;

import org.jfaster.mango.util.Strings;

/**
 * @author ash
 */
public class Order {

  public static final Direction DEFAULT_DIRECTION = Direction.ASC;

  private final Direction direction;
  private final String property;

  private Order(Direction direction, String property) {
    if (direction == null) {
      throw new IllegalArgumentException("direction must not be null");
    }
    if (Strings.isEmpty(property)) {
      throw new IllegalArgumentException("property must not be null or empty");
    }

    this.direction = direction;
    this.property = property;
  }

  public static Order by(Direction direction, String property) {
    return new Order(direction, property);
  }

  public static Order by(String property) {
    return new Order(DEFAULT_DIRECTION, property);
  }

  public static Order asc(String property) {
    return new Order(Direction.ASC, property);
  }

  public static Order desc(String property) {
    return new Order(Direction.DESC, property);
  }

  public Direction getDirection() {
    return direction;
  }

  public String getProperty() {
    return property;
  }

  @Override
  public String toString() {
    return String.format("%s %s", property, direction.toString().toLowerCase());
  }

}

