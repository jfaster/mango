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

package org.jfaster.mango.support;

import org.jfaster.mango.DbTest;
import org.jfaster.mango.util.ScriptRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author ash
 */
public enum Table {

  USER("user.sql"),
  PERSON("person.sql"),
  BYTE_INFO("byte_info.sql"),
  MSG("msg.sql"),
  MSG_PARTITION("msg_partition.sql"),
  MSG_ROUTER1("msg_router1.sql"),
  MSG_ROUTER2("msg_router2.sql"),
  MSG_ROUTER3("msg_router3.sql"),
  LONG_ID_MSG("long_id_msg.sql"),
  ACCOUNT("account.sql"),
  POSITION("position.sql"),
  BT("bt.sql"),
  BILL_PARTITION("bill_partition.sql"),
  ORDER("order.sql"),
  ORDER_PARTITION("order_partition.sql"),
  PRODUCT_PARTITION("product_partition.sql"),
  TABLE_INCLUDE_ALL_TYPES("table_include_all_types.sql"),
  ;

  private String name;

  private Table(String name) {
    this.name = name;
  }

  public void load(Connection conn) throws IOException, SQLException {
    ScriptRunner sr = new ScriptRunner(conn, false, true);
    InputStream is = DbTest.class.getResourceAsStream("/" + DataSourceConfig.getDir() + "/" + name);
    sr.runScript(new InputStreamReader(is));
  }

  public void load(DataSource ds) throws IOException, SQLException {
    Connection conn = ds.getConnection();
    load(conn);
    conn.close();
  }

}
