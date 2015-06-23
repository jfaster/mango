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

import org.jfaster.mango.exception.IncorrectParameterCountException;
import org.jfaster.mango.exception.IncorrectParameterTypeException;
import org.jfaster.mango.exception.NotReadableParameterException;
import org.jfaster.mango.reflect.ParameterDescriptor;
import org.jfaster.mango.reflect.TypeToken;
import org.jfaster.mango.support.model4table.User;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class ParameterContextTest {

    @Test
    public void testSelectAndUpdate() throws Exception {
        List<Annotation> empty = Collections.emptyList();
        TypeToken<String> t0 = new TypeToken<String>() {};
        ParameterDescriptor p0 = new ParameterDescriptor(0, t0.getType(), empty, "1");
        TypeToken<Integer> t1 = new TypeToken<Integer>() {};
        ParameterDescriptor p1 = new ParameterDescriptor(1, t1.getType(), empty, "2");
        List<ParameterDescriptor> pds = Arrays.asList(p0, p1);
        NameProvider np = new NameProvider(pds);

        ParameterContext ctx = new ParameterContext(pds, np, OperatorType.QUERY);
        assertThat(ctx.getInvokerGroup("1", "").getFinalType(), equalTo(t0.getType()));
        assertThat(ctx.getInvokerGroup("2", "").getFinalType(), equalTo(t1.getType()));
        assertThat(ctx.getParameterDescriptors(), equalTo(pds));
    }

    @Test
    public void testBatchUpdate() throws Exception {
        List<Annotation> empty = Collections.emptyList();
        TypeToken<List<User>> t = new TypeToken<List<User>>() {};
        ParameterDescriptor p = new ParameterDescriptor(0, t.getType(), empty, "1");
        List<ParameterDescriptor> pds = Arrays.asList(p);
        NameProvider np = new NameProvider(pds);

        ParameterContext ctx = new ParameterContext(pds, np, OperatorType.BATCHUPDATYPE);
        TypeToken<User> ut = TypeToken.of(User.class);
        ParameterDescriptor up = new ParameterDescriptor(0, ut.getType(), empty, "1");
        assertThat(ctx.getInvokerGroup("1", "").getFinalType(), equalTo(ut.getType()));
        assertThat(ctx.getInvokerGroup("1", "").getFinalType(), equalTo(ut.getType())); // test cache
        assertThat(ctx.getInvokerGroup("1", "name").getFinalType(), equalTo((new TypeToken<String>() {}).getType()));
        assertThat(ctx.getInvokerGroup("1", "name").getFinalType(), equalTo((new TypeToken<String>() {}).getType())); // test cache
        assertThat(ctx.getParameterDescriptors(), equalTo(Arrays.asList(up)));
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testIncorrectParameterCountException() throws Exception {
        thrown.expect(IncorrectParameterCountException.class);
        thrown.expectMessage("batch update expected one and only one parameter but 2");

        List<Annotation> empty = Collections.emptyList();
        TypeToken<String> t0 = new TypeToken<String>() {};
        ParameterDescriptor p0 = new ParameterDescriptor(0, t0.getType(), empty, "1");
        TypeToken<Integer> t1 = new TypeToken<Integer>() {};
        ParameterDescriptor p1 = new ParameterDescriptor(1, t1.getType(), empty, "2");
        List<ParameterDescriptor> pds = Arrays.asList(p0, p1);
        NameProvider np = new NameProvider(pds);

        new ParameterContext(pds, np, OperatorType.BATCHUPDATYPE);
    }

    @Test
    public void testIncorrectParameterTypeException() throws Exception {
        thrown.expect(IncorrectParameterTypeException.class);
        thrown.expectMessage("parameter of batch update expected array or " +
                "implementations of java.util.List or " +
                "implementations of java.util.Set but class java.lang.String");

        List<Annotation> empty = Collections.emptyList();
        TypeToken<String> t = new TypeToken<String>() {};
        ParameterDescriptor p = new ParameterDescriptor(0, t.getType(), empty, "1");
        List<ParameterDescriptor> pds = Arrays.asList(p);
        NameProvider np = new NameProvider(pds);

        new ParameterContext(pds, np, OperatorType.BATCHUPDATYPE);
    }

    @Test
    public void testNotReadableParameterException() throws Exception {
        thrown.expect(NotReadableParameterException.class);
        thrown.expectMessage("parameter :2 is not readable");

        List<Annotation> empty = Collections.emptyList();
        TypeToken<String> t = new TypeToken<String>() {};
        ParameterDescriptor p = new ParameterDescriptor(0, t.getType(), empty, "1");
        List<ParameterDescriptor> pds = Arrays.asList(p);
        NameProvider np = new NameProvider(pds);

        ParameterContext ctx = new ParameterContext(pds, np, OperatorType.UPDATE);
        ctx.getInvokerGroup("2", "");
    }

}
