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
import org.jfaster.mango.stat.OperatorStat;
import org.jfaster.mango.stat.StatInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ash
 */
public class StatsRender {

  public static String getHtml(boolean isFetchAll) throws Exception {
    List<Mango> mangos = Mango.getInstances();
    if (mangos.size() != 1) {
      throw new IllegalStateException("instance of mango expected 1 but " + mangos.size());
    }
    Mango mango = mangos.get(0);
    Map<String, OperatorStat> osMap = new HashMap<String, OperatorStat>();
    Map<String, ExtendStat> esMap = new HashMap<String, ExtendStat>();
    StatInfo info = mango.getStatInfo();
    int index = 0;
    for (OperatorStat os : info.getStats()) {
      osMap.put(String.valueOf(index), os);
      esMap.put(String.valueOf(index), new ExtendStat(os));
      index++;
    }
    String html = Template.render(format(info.getStatBeginTime()), format(info.getStatEndTime()), osMap, esMap, isFetchAll);
    return html;
  }

  private static String format(long time) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return format.format(new Date(time));
  }

}