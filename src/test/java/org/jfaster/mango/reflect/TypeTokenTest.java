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

package org.jfaster.mango.reflect;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author ash
 */
public class TypeTokenTest {

    private abstract static class StringList implements List<String> {}

    @Test
    public void testGetType() throws Exception {
        TypeToken<List<String>> token = new TypeToken<List<String>>() {};
        assertThat(token.getType(), equalTo(StringList.class.getGenericInterfaces()[0]));

        TypeToken<String> token2 = new TypeToken<String>() {};
        assertThat(token2.getType().equals(String.class), is(true));
    }

    @Test
    public void testGetRawType() throws Exception {
        TypeToken<List<String>> token = new TypeToken<List<String>>() {};
        assertThat(token.getRawType().equals(List.class), is(true));

        TypeToken<String> token2 = new TypeToken<String>() {};
        assertThat(token2.getRawType().equals(String.class), is(true));
    }

    @Test
    public void testOf() throws Exception {
        TypeToken<String> token = TypeToken.of(String.class);
        assertThat(token.getType().equals(String.class), is(true));
        assertThat(token.getRawType().equals(String.class), is(true));
    }


}
