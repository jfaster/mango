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

import org.jfaster.mango.operator.cache.CacheExpire;

import java.lang.annotation.*;

/**
 * 指明该DAO需要集成cache
 *
 * @author ash
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {

  /**
   * 缓存key前缀
   *
   * @return
   */
  String prefix();

  /**
   * 缓存过期时间单位
   *
   * @return
   */
  Class<? extends CacheExpire> expire();

  /**
   * 缓存过期时间数量
   *
   * @return
   */
  int num() default 1;

  /**
   * 是否缓存null对象
   *
   * @return
   */
  boolean cacheNullObject() default false;

  /**
   * 是否缓存空列表
   *
   * @return
   */
  boolean cacheEmptyList() default true;

}
