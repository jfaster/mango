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

package org.jfaster.mango.proxy;

import org.jfaster.mango.util.reflect.AbstractInvocationHandler;
import org.jfaster.mango.util.reflect.Reflection;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @author ash
 */
public class ProxyTest {

  @Test
  public void test() {
    MyInvocationHandler handler = new MyInvocationHandler();
    Item item1 = Reflection.newProxy(SubItem1.class, handler);
    Item item2 = Reflection.newProxy(SubItem2.class, handler);

    item1.add();
    item2.add();

  }


  private static class MyInvocationHandler extends AbstractInvocationHandler {

    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
      System.out.println(method);
      return null;
    }

  }

  interface Item {
    String add();
  }

  interface SubItem1 extends Item {
  }

  interface SubItem2 extends Item {
  }

}
