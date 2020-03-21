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

package org.jfaster.mango.crud;

import org.jfaster.mango.crud.named.factory.NamedCountBuilderFactory;
import org.jfaster.mango.crud.named.factory.NamedDeleteBuilderFactory;
import org.jfaster.mango.crud.named.factory.NamedQueryBuilderFactory;
import org.jfaster.mango.descriptor.MethodDescriptor;
import org.jfaster.mango.descriptor.SqlGenerator;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class NamedSqlGenerator implements SqlGenerator {

  private static final List<BuilderFactory> customBuilderFactories = new ArrayList<>();
  static {
    customBuilderFactories.add(new NamedQueryBuilderFactory());
    customBuilderFactories.add(new NamedCountBuilderFactory());
    customBuilderFactories.add(new NamedDeleteBuilderFactory());
  }

  @Override
  @Nullable
  public String generateSql(MethodDescriptor md) {
    String sql = null;
    Builder b = getBuilder(md);
    if (b != null) {
      sql = b.buildSql();
    }
    return sql;
  }

  @Nullable
  private Builder getBuilder(MethodDescriptor md) {
    for (BuilderFactory lookupBuilderFactory : customBuilderFactories) {
      Builder builder = lookupBuilderFactory.tryGetBuilder(md);
      if (builder != null) {
        return builder;
      }
    }
    return null;
  }

}
