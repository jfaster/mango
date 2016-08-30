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

import java.lang.annotation.*;

/**
 * 用此注解修饰的方法参数或参数中的某个属性将作为缓存key的后缀<br>
 * {@link Cache#prefix()}为缓存key的前缀<br>
 * 完整的缓存key=缓存key前缀＋缓存key后缀
 *
 * @author ash
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheBy {

  /**
   * 如果value等于""，被修饰的参数直接作为缓存后缀<br>
   * 如果value不等于""，则被修饰参数中的value值属性将作为缓存后缀
   *
   * @return
   */
  String value() default "";

}
