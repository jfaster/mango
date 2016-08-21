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

package org.jfaster.mango.invoker;

import org.junit.Test;

import java.lang.reflect.Type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class UnreachablePropertyExceptionTest {

    @Test
    public void test() throws Exception {
        UnreachablePropertyException e = new UnreachablePropertyException(String.class, "abc");
        assertThat(e.getMessage(), equalTo("The property 'abc' of 'class java.lang.String' is unreachable"));
        assertThat(e.getOriginalType(), equalTo((Type) String.class));
        assertThat(e.getCurrentType(), equalTo((Type) String.class));
        assertThat(e.getUnreachableProperty(), equalTo("abc"));
        assertThat(e.getUnreachablePropertyPath(), equalTo("abc"));

        e = new UnreachablePropertyException(String.class, Integer.class, "xyz", "abc.xyz");
        assertThat(e.getMessage(), equalTo("The property 'abc.xyz' of 'class java.lang.String' is unreachable"));
        assertThat(e.getOriginalType(), equalTo((Type) String.class));
        assertThat(e.getCurrentType(), equalTo((Type) Integer.class));
        assertThat(e.getUnreachableProperty(), equalTo("xyz"));
        assertThat(e.getUnreachablePropertyPath(), equalTo("abc.xyz"));
    }

}
