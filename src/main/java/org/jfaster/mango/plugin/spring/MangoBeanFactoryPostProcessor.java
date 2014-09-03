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

import org.jfaster.mango.annotation.DB;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author ash
 */
public class MangoBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private static final String DAO_END = "Dao";

    private List<String> packages;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        DefaultListableBeanFactory dlbf = (DefaultListableBeanFactory) beanFactory;
        for (Class<?> daoClass : findMangoDaoClass()) {
            GenericBeanDefinition bf = new GenericBeanDefinition();
            bf.setBeanClassName(daoClass.getName());
            MutablePropertyValues pvs = bf.getPropertyValues();
            pvs.addPropertyValue("daoClass", daoClass);
            bf.setBeanClass(MangoBeanFactory.class);
            bf.setPropertyValues(pvs);
            bf.setLazyInit(false);
            dlbf.registerBeanDefinition(daoClass.getName(), bf);
        }
    }

    private List<Class<?>> findMangoDaoClass() {
        try {
            ClassPath cp = ClassPath.from(MangoBeanFactoryPostProcessor.class.getClassLoader());
            Set<ClassPath.ClassInfo> tlcs = cp.getTopLevelClasses();
            List<Class<?>> mangoDaoClasses = new LinkedList<Class<?>>();
            for (ClassPath.ClassInfo info : tlcs) {
                for (String p : packages) {
                    String className = info.getClassName();
                    if (className.startsWith(p + ".") && className.endsWith(DAO_END)) {
                        Class<?> mangoDaoClass = info.load();
                        if (mangoDaoClass.isInterface() && mangoDaoClass.getAnnotation(DB.class) != null) {
                            mangoDaoClasses.add(mangoDaoClass);
                        }
                    }
                }
            }
            return mangoDaoClasses;
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

}
