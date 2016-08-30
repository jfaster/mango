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

package org.jfaster.mango.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

/**
 * 将collection和数组的迭代结合在一起
 *
 * @author ash
 */
public class Iterables implements Iterable {

  private Collection<?> collection = null;
  private Object array = null;
  private Integer size = null;
  private Object object = null;

  public Iterables(Object object) {
    if (Collection.class.isAssignableFrom(object.getClass())) { // 集合
      this.collection = (Collection<?>) object;
    } else if (object.getClass().isArray()) { // 数组
      this.array = object;
    }
    this.object = object;
  }

  public boolean isIterable() {
    return collection != null || array != null;
  }

  public boolean isCollection() {
    return collection != null;
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public int size() {
    if (size == null) {
      if (collection != null) {
        size = collection.size();
      } else if (array != null) {
        size = Array.getLength(array);
      } else {
        throw new IllegalArgumentException("class need collection or array but " + object.getClass());
      }
    }
    return size;
  }

  @Override
  public Iterator iterator() {
    if (collection != null) {
      return collection.iterator();
    } else if (array != null) {
      return new ArrayItr();
    } else {
      throw new IllegalArgumentException("class need collection or array but " + object.getClass());
    }
  }

  private class ArrayItr implements Iterator {

    private int cursor = 0;

    @Override
    public boolean hasNext() {
      return cursor != size();
    }

    @Override
    public Object next() {
      return Array.get(array, cursor++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

}
