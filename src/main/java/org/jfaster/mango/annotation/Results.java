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
 * 自定义数据库表字段到类属性的映射，和{@link Result}配合使用
 * <p/>
 * <p>比如我们需要将数据库user表中的user_id,user_name字段，分别映射到类User的id,name属性
 * <p/>
 * <pre>
 * interface UserDao {
 *
 *   {@literal@}Results({
 *     {@literal@}Result(column = "user_id", property = "id"),
 *     {@literal@}Result(column = "user_name", property = "name")
 *   })
 *   {@literal@}SQL("select user_id, user_name from user where user_id = :1")
 *   public User getUserById(int id);
 *
 *   {@literal@}Results({
 *     {@literal@}Result(column = "user_id", property = "id"),
 *     {@literal@}Result(column = "user_name", property = "name")
 *   })
 *   {@literal@}SQL("select user_id, user_name from user where user_name = :1")
 *   public User getUserByName(String name);
 *
 * }
 * </pre>
 * <p/>
 * <P>我们还可以把该注解放在UserDao上，效果和上面的代码一样
 * <p/>
 * <pre>
 * {@literal@}Results({
 *   {@literal@}Result(column = "user_id", property = "id"),
 *   {@literal@}Result(column = "user_name", property = "name")
 * })
 * interface UserDao {
 *
 *   {@literal@}SQL("select user_id, user_name from user where user_id = :1")
 *   public User getUserById(int id);
 *
 *   {@literal@}SQL("select user_id, user_name from user where user_name = :1")
 *   public User getUserByName(String name);
 *
 * }
 * </pre>
 *
 * @author ash
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Results {

  Result[] value() default {};

}
