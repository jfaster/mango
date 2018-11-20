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

package org.jfaster.mango.plugin.page;

/**
 * @author ash
 */
public class Page {

  private static final int DEFAULT_PAGE_NUM = 0; // 默认查询第1页

  private static final int DEFAULT_PAGE_SIZE = 20; // 默认数据数量20

  private static final boolean DEFAULT_IS_FETCH_TOTAL = false; // 默认不取总数

  private final int pageNum;

  private final int pageSize;

  private final boolean isFetchTotal;

  private int total;

  private Page(int pageNum, int pageSize, boolean isFetchTotal) {
    this.pageNum = pageNum;
    this.pageSize = pageSize;
    this.isFetchTotal = isFetchTotal;
  }

  public static Page create(int pageNum, int pageSize, boolean isFetchTotal) {
    return new Page(pageNum, pageSize, isFetchTotal);
  }

  public static Page create(int pageNum, int pageSize) {
    return create(pageNum, pageSize, DEFAULT_IS_FETCH_TOTAL);
  }

  public static Page create() {
    return create(DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE);
  }

  public static Page create(boolean isFetchTotal) {
    return new Page(DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE, isFetchTotal);
  }

  public boolean isFetchTotal() {
    return isFetchTotal;
  }

  public int getPageNum() {
    return pageNum;
  }

  public int getPageSize() {
    return pageSize;
  }

  public int getTotal() {
    if (!isFetchTotal) {
      throw new PageException("can't fetch total, please set isFetchTotal to true");
    }
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }
}
