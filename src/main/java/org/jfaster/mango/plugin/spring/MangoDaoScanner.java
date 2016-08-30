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
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ash
 */
final public class MangoDaoScanner implements BeanFactoryPostProcessor {

  private final static InternalLogger logger = InternalLoggerFactory.getInstance(MangoDaoScanner.class);

  private static final List<String> DAO_ENDS = Arrays.asList("Dao", "DAO");

  List<String> locationPatterns = new ArrayList<String>();

  Class<?> factoryBeanClass = DefaultMangoFactoryBean.class;

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    DefaultListableBeanFactory dlbf = (DefaultListableBeanFactory) beanFactory;
    for (Class<?> daoClass : findMangoDaoClasses()) {
      GenericBeanDefinition bf = new GenericBeanDefinition();
      bf.setBeanClassName(daoClass.getName());
      MutablePropertyValues pvs = bf.getPropertyValues();
      pvs.addPropertyValue("daoClass", daoClass);
      bf.setBeanClass(factoryBeanClass);
      bf.setPropertyValues(pvs);
      bf.setLazyInit(false);
      dlbf.registerBeanDefinition(daoClass.getName(), bf);
    }
  }

  private List<Class<?>> findMangoDaoClasses() {
    try {
      List<Class<?>> daos = new ArrayList<Class<?>>();
      ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
      MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
      for (String locationPattern : locationPatterns) {
        Resource[] rs = resourcePatternResolver.getResources(locationPattern);
        for (Resource r : rs) {
          MetadataReader reader = metadataReaderFactory.getMetadataReader(r);
          AnnotationMetadata annotationMD = reader.getAnnotationMetadata();
          if (annotationMD.hasAnnotation(DB.class.getName())) {
            ClassMetadata clazzMD = reader.getClassMetadata();
            daos.add(Class.forName(clazzMD.getClassName()));
          }
        }
      }
      return daos;
    } catch (Exception e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }

  public void setPackages(List<String> packages) {
    for (String p : packages) {
      for (String daoEnd : DAO_ENDS) {
        String locationPattern = "classpath*:" + p.replaceAll("\\.", "/") + "/**/*" + daoEnd + ".class";
        logger.info("trnas package[" + p + "] to locationPattern[" + locationPattern + "]");
        locationPatterns.add(locationPattern);
      }
    }
  }

  public void setFactoryBeanClass(Class<?> factoryBeanClass) {
    this.factoryBeanClass = factoryBeanClass;
  }
}
