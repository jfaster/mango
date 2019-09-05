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

package org.jfaster.mango.util.reflect;

import org.jfaster.mango.page.PageResult;

import java.util.List;
import java.util.Optional;

/**
 * @author ash
 */
public class DynamicTokens {

  public static <E> TypeToken<Iterable<E>> iterableToken(TypeToken<E> entityToken) {
    return new TypeToken<Iterable<E>>() {}
        .where(new TypeParameter<E>() {}, entityToken);
  }

  public static <E> TypeToken<List<E>> listToken(TypeToken<E> entityToken) {
    return new TypeToken<List<E>>() {}
        .where(new TypeParameter<E>() {}, entityToken);
  }

  public static <E> TypeToken<Optional<E>> optionalToken(TypeToken<E> entityToken) {
    return new TypeToken<Optional<E>>() {}
        .where(new TypeParameter<E>() {}, entityToken);
  }

  public static <E> TypeToken<PageResult<E>> pageResultToken(TypeToken<E> entityToken) {
    return new TypeToken<PageResult<E>>() {}
        .where(new TypeParameter<E>() {}, entityToken);
  }

}
