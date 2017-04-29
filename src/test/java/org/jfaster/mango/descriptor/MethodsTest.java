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

package org.jfaster.mango.descriptor;

import org.jfaster.mango.util.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class MethodsTest {

  @Test
  public void testResolveType() throws Exception {
    TypeToken<?> daoTypeToken = TypeToken.of(SubDao.class);

    Method m = SubDao.class.getMethod("add", Object.class);
    Type type = Methods.fixAndResolveType(m.getGenericReturnType(), daoTypeToken);
    assertThat(type, equalTo((Type) void.class));
    type = Methods.fixAndResolveType(m.getGenericParameterTypes()[0], daoTypeToken);
    assertThat(type, equalTo((Type) String.class));

    m = SubDao.class.getMethod("add", Collection.class);
    type = Methods.fixAndResolveType(m.getGenericReturnType(), daoTypeToken);
    assertThat(TypeToken.of(type).getRawType(), equalTo((Type) int[].class));
    type = Methods.fixAndResolveType(m.getGenericParameterTypes()[0], daoTypeToken);
    assertThat(type, equalTo((new TypeToken<Collection<String>>(){}.getType())));

    m = SubDao.class.getMethod("findOne", Object.class);
    type = Methods.fixAndResolveType(m.getGenericReturnType(), daoTypeToken);
    assertThat(type, equalTo((Type) String.class));
    type = Methods.fixAndResolveType(m.getGenericParameterTypes()[0], daoTypeToken);
    assertThat(type, equalTo((Type) Integer.class));

    m = SubDao.class.getMethod("findAll", List.class);
    type = Methods.fixAndResolveType(m.getGenericReturnType(), daoTypeToken);
    assertThat(type, equalTo((new TypeToken<List<String>>(){}.getType())));
    type = Methods.fixAndResolveType(m.getGenericParameterTypes()[0], daoTypeToken);
    assertThat(type, equalTo((new TypeToken<List<Integer>>(){}.getType())));

    m = SubDao.class.getMethod("update", Object.class);
    type = Methods.fixAndResolveType(m.getGenericReturnType(), daoTypeToken);
    assertThat(type, equalTo((Type) int.class));
    type = Methods.fixAndResolveType(m.getGenericParameterTypes()[0], daoTypeToken);
    assertThat(type, equalTo((Type) String.class));

    m = SubDao.class.getMethod("update", Collection.class);
    type = Methods.fixAndResolveType(m.getGenericReturnType(), daoTypeToken);
    assertThat(TypeToken.of(type).getRawType(), equalTo((Type) int[].class));
    type = Methods.fixAndResolveType(m.getGenericParameterTypes()[0], daoTypeToken);
    assertThat(type, equalTo((new TypeToken<Collection<String>>(){}.getType())));

    m = SubDao.class.getMethod("delete", Object.class);
    type = Methods.fixAndResolveType(m.getGenericReturnType(), daoTypeToken);
    assertThat(type, equalTo((Type) int.class));
    type = Methods.fixAndResolveType(m.getGenericParameterTypes()[0], daoTypeToken);
    assertThat(type, equalTo((Type) Integer.class));

    m = SubDao.class.getMethod("getDate", List.class);
    type = Methods.fixAndResolveType(m.getGenericReturnType(), daoTypeToken);
    assertThat(type, equalTo((Type) Date.class));
    type = Methods.fixAndResolveType(m.getGenericParameterTypes()[0], daoTypeToken);
    assertThat(type, equalTo((new TypeToken<List<String>>(){}.getType())));

  }

  interface SuperDao<T, ID> extends Generic<T, ID> {
    void add(T entity);

    int[] add(Collection<T> entities);

    T findOne(ID id);

    List<T> findAll(List<ID> ids);

    int update(T entity);

    int[] update(Collection<T> entities);

    int delete(ID id);
  }

  interface SubDao extends SuperDao<String, Integer> {

    Date getDate(List<String> t);

  }

}