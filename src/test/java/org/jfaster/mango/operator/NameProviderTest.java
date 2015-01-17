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

package org.jfaster.mango.operator;

import org.jfaster.mango.reflect.ParameterDescriptor;
import org.jfaster.mango.reflect.TypeToken;
import org.jfaster.mango.support.MockRename;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class NameProviderTest {

    @Test
    public void testGetParameterName() throws Exception {
        int num = 10;
        Map<Integer, String> map = new HashMap<Integer, String>();
        map.put(1, "ash");
        map.put(4, "lucy");
        map.put(8, "lily");
        TypeToken<String> token = TypeToken.of(String.class);
        List<ParameterDescriptor> pds = new ArrayList<ParameterDescriptor>();
        for (int i = 0; i < num; i++) {
            List<Annotation> annos = new ArrayList<Annotation>();
            String name = map.get(i);
            if (name != null) {
                annos.add(new MockRename(name));
            }
            pds.add(new ParameterDescriptor(i, token.getType(), annos, String.valueOf(i + 1)));
        }

        NameProvider np = new NameProvider(pds);
        for (int i = 0; i < num; i++) {
            String name = map.get(i);
            assertThat(np.getParameterName(i), equalTo(name != null ? name : String.valueOf(i + 1)));
        }
    }

}
