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

package org.jfaster.mango.plugin.spring;

import org.jfaster.mango.operator.Mango;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author ash
 */
public abstract class AbstractMangoFactoryBean implements FactoryBean {

  private Class<?> daoClass;

  private static volatile Mango mango;

  public abstract Mango createMango();

  public Object createDao(Mango mango, Class<?> daoClass) {
    return mango.create(daoClass);
  }

  @Override
  public Object getObject() throws Exception {
    Mango mango = getMango();
    return mango.create(daoClass);
  }

  @Override
  public Class<?> getObjectType() {
    return daoClass;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public Mango getMango() {
    if (mango == null) {
      synchronized (AbstractMangoFactoryBean.class) {
        if (mango == null) {
          mango = createMango();
        }
      }
    }
    return mango;
  }

  public void setDaoClass(Class<?> daoClass) {
    this.daoClass = daoClass;
  }

}
