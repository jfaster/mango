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

package org.jfaster.mango.util.reflect;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author ash
 */
public class TypeWrapper {

  /**
   * type映射的原始类型
   * <p/>
   * 如果type为{@code Integer}，它的值为{@code Integer.class}，
   * 如果type为{@code List<Integer>}，它的值为{@code Integer.class}，
   * 如果type为{@code List<List<String>>}，{@code List<T>>}等，它的值为{@code null}
   */
  private Class<?> mappedClass;

  /**
   * type映射的原始类型，抛出异常时需要用到
   */
  private Type mappedType;

  private boolean isArray;
  private boolean isCollection;
  private boolean isList;
  private boolean isArrayList;
  private boolean isLinkedList;
  private boolean isSet;
  private boolean isHashSet;
  private boolean isCollectionAssignable;

  public TypeWrapper(final Type type) {
    if (byte[].class.equals(type) || Byte[].class.equals(type)) { // byte[]和Byte[]是jdbc中的一个基础类型,所以不把它作为数组处理
      mappedType = mappedClass = (Class<?>) type;
    } else {
      new TypeVisitor() {
        @Override
        void visitClass(Class<?> t) {
          mappedType = t;
          if (t.isArray()) { // 数组
            isArray = true;
            mappedType = t.getComponentType();
          }
        }

        @Override
        void visitGenericArrayType(GenericArrayType t) {
          throw new IllegalStateException("Does not support the generic array type " + type);
        }

        @Override
        void visitParameterizedType(ParameterizedType t) {
          Type rawType = t.getRawType();

          // 支持Collection,List,ArrayList,LinkedList,Set,HashSet
          if (Collection.class.equals(rawType)) {
            isCollection = true;
          } else if (List.class.equals(rawType)) {
            isList = true;
          } else if (ArrayList.class.equals(rawType)) {
            isArrayList = true;
          } else if (LinkedList.class.equals(rawType)) {
            isLinkedList = true;
          } else if (Set.class.equals(rawType)) {
            isSet = true;
          } else if (HashSet.class.equals(rawType)) {
            isHashSet = true;
          } else {
            throw new IllegalStateException("parameterized type must be one of" +
                "[Collection,List,ArrayList,LinkedList,Set,HashSet] but " + type);
          }
          isCollectionAssignable = true;
          mappedType = t.getActualTypeArguments()[0];
        }

        @Override
        void visitTypeVariable(TypeVariable<?> t) {
          throw new IllegalStateException("Does not support the type variable " + type);
        }

        @Override
        void visitWildcardType(WildcardType t) {
          throw new IllegalStateException("Does not support the wildcard type " + type);
        }
      }.visit(type);
      mappedClass = TypeToken.of(mappedType).getRawType();
    }
  }

  public boolean isArray() {
    return isArray;
  }

  public boolean isCollection() {
    return isCollection;
  }

  public boolean isList() {
    return isList;
  }

  public boolean isArrayList() {
    return isArrayList;
  }

  public boolean isLinkedList() {
    return isLinkedList;
  }

  public boolean isSet() {
    return isSet;
  }

  public boolean isHashSet() {
    return isHashSet;
  }

  public boolean isIterable() {
    return isCollectionAssignable || isArray;
  }

  public Class<?> getMappedClass() {
    return mappedClass;
  }

  public Type getMappedType() {
    return mappedType;
  }

}










