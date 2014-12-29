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

package org.jfaster.mango.invoker.function;

import javax.annotation.Nullable;
import java.util.List;

/**
 * List<Integer> --> String
 *
 * @author ash
 */
public class IntegerListToStringFunction extends LiteFunction<List<Integer>, String> {

    private final static String SEPARATOR = ",";

    @Nullable
    @Override
    public String apply(@Nullable List<Integer> input) {
        if (input == null || input.size() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(input.size() * 5);
        builder.append(input.get(0));
        for (int i = 1; i < input.size(); i++) {
            builder.append(SEPARATOR).append(input.get(i));
        }
        return builder.toString();
    }

}
