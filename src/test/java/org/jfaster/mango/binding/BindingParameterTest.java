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

package org.jfaster.mango.binding;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class BindingParameterTest {

  @Test
  public void testGetFullName() throws Exception {
    BindingParameter bp = BindingParameter.create("a", "b", null);
    assertThat(bp.getFullName(), equalTo(":a.b"));
    bp = BindingParameter.create("a", "", null);
    assertThat(bp.getFullName(), equalTo(":a"));

  }

  @Test
  public void testEquals() throws Exception {
    BindingParameter bp = BindingParameter.create("a", "b", null);
    BindingParameter bp2 = BindingParameter.create("a", "b", null);
    assertThat(bp.equals(bp2), equalTo(true));
    assertThat(bp.equals(null), equalTo(false));
    assertThat(bp.equals(new Object()), equalTo(false));
  }

}
