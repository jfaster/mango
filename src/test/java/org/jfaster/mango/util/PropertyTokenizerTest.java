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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class PropertyTokenizerTest {

  @Test
  public void test() throws Exception {
    PropertyTokenizer prop = new PropertyTokenizer("a.b.c");
    assertThat(prop.getName(), equalTo("a"));
    assertThat(prop.getChildren(), equalTo("b.c"));

    PropertyTokenizer prop2 = new PropertyTokenizer("a");
    assertThat(prop2.getName(), equalTo("a"));
    assertThat(prop2.getChildren(), nullValue());
  }

}
