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

import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class DynamicTokensTest {

  @Test
  public void testCollectionToken() throws Exception {
    TypeToken<Collection<String>> token = DynamicTokens.collectionToken(TypeToken.of(String.class));
    Type expectedType = DynamicTokensTest.class.getMethod("func").getGenericReturnType();
    assertThat(token.getType(), equalTo(expectedType));
  }

  @Test
  public void testListToken() throws Exception {
    TypeToken<List<String>> token = DynamicTokens.listToken(TypeToken.of(String.class));
    Type expectedType = DynamicTokensTest.class.getMethod("func2").getGenericReturnType();
    assertThat(token.getType(), equalTo(expectedType));
  }

  public Collection<String> func() {
    return null;
  }

  public List<String> func2() {
    return null;
  }

}