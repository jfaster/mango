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

import org.jfaster.mango.util.Joiner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ash
 */
public class Sort {

  private static final Sort UNSORTED = Sort.by(new Order[0]);

  private final List<Order> orders;

  private Sort(List<Order> orders) {
    this.orders = Collections.unmodifiableList(orders);
  }

  public static Sort by(List<Order> orders) {
    if (orders == null) {
      throw new IllegalArgumentException("orders must not be null");
    }
    return new Sort(orders);
  }

  public static Sort by(Order... orders) {
    if (orders == null) {
      throw new IllegalArgumentException("orders must not be null");
    }
    return Sort.by(Arrays.asList(orders));
  }

  public static Sort by(String... properties) {
    if (properties == null) {
      throw new IllegalArgumentException("properties must not be null");
    }
    return properties.length == 0 ? unsorted() : Sort.by(Order.DEFAULT_DIRECTION, properties);
  }

  public static Sort by(Direction direction, String... properties) {
    if (direction == null) {
      throw new IllegalArgumentException("direction must not be null");
    }
    if (properties == null || properties.length == 0) {
      throw new IllegalArgumentException("properties must not be null or empty");
    }
    return Sort.by(Arrays.stream(properties)//
        .map(it -> Order.by(direction, it))//
        .collect(Collectors.toList()));
  }

  public List<Order> getOrders() {
    return orders;
  }

  private static Sort unsorted() {
    return UNSORTED;
  }

  @Override
  public String toString() {
    if (orders.isEmpty()) {
      return "";
    }
    return " order by " + Joiner.on(", ").join(orders);
  }

}
