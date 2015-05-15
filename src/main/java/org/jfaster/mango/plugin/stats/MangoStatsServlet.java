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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * @author ash
 */
public class MangoStatsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("content-type", "text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            Class.forName("freemarker.template.Template");
        } catch (ClassNotFoundException e) {
            out.println("need freemarker");
            out.flush();
            return;
        }

        try {
            String type = req.getParameter("type");
            if ("reset".equals(type)) {
                List<Mango> mangos = Mango.getInstances();
                if (mangos.size() != 1) {
                    throw new IllegalStateException("instance of mango expected 1 but " + mangos.size());
                }
                mangos.get(0).resetAllStats();
            }
            out.println(StatsRender.getHtml(Boolean.valueOf(req.getParameter("all"))));
            out.flush();
        } catch (Exception e) {
            out.println(e.getMessage());
            out.flush();
        }
    }

}
