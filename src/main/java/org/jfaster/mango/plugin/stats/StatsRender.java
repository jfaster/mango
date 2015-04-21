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

import freemarker.template.Template;
import org.jfaster.mango.operator.Mango;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ash
 */
public class StatsRender {

    public static String getHtml(boolean isFetchAll) throws Exception {
        InputStream is = null;
        InputStreamReader reader = null;
        StringWriter sw = null;
        BufferedWriter bw = null;
        try {
            is = StatsRender.class.getResourceAsStream("stats.ftl");
            reader = new InputStreamReader(is);
            sw = new StringWriter();
            bw = new BufferedWriter(sw);
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("mangos", Mango.getInstances());
            data.put("isFetchAll", isFetchAll);
            Template t = new Template(null, reader, null);
            t.process(data, bw);
            bw.flush();
            return sw.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (is != null) {
                is.close();
            }
            if (bw != null) {
                bw.close();
            }
            if (sw != null) {
                sw.close();
            }
        }
    }

}
