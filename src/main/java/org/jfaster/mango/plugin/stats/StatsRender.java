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

package org.jfaster.mango.plugin.stats;

import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.stat.OperatorStats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ash
 */
public class StatsRender {

  public static String getHtml(boolean isFetchAll, String key) throws Exception {
    List<Mango> mangos = Mango.getInstances();
    if (mangos.size() != 1) {
      throw new IllegalStateException("instance of mango expected 1 but " + mangos.size());
    }
    Mango mango = mangos.get(0);
    Map<String, OperatorStats> osMap = new HashMap<String, OperatorStats>();
    Map<String, ExtendStats> esMap = new HashMap<String, ExtendStats>();
    int index = 0;
    for (OperatorStats os : mango.getAllStats()) {
      osMap.put(String.valueOf(index), os);
      esMap.put(String.valueOf(index), new ExtendStats(os));
      index++;
    }
    String html = Template.render(osMap, esMap, isFetchAll, key);
    return html;
  }
}