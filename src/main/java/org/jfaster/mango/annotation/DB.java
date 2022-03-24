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

package org.jfaster.mango.annotation;

import org.jfaster.mango.datasource.AbstractDataSourceFactory;

import java.lang.annotation.*;

/**
 * 修饰DAO接口，只有使用此注解修饰的DAO接口，才能被mango识别
 *
 * @author ash
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DB {

  /**
   * 数据源工厂名
   *
   * @return
   */
  String name() default AbstractDataSourceFactory.DEFAULT_NAME;

  /**
   * 全局表名，在{@link SQL}的字符串参数，可以通过#table的方式引用此全局表名。
   *
   * @return
   */
  String table() default "";

}
