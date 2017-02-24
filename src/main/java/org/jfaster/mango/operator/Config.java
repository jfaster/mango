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

package org.jfaster.mango.operator;

/**
 * mango的一些扩展配置信息
 *
 * @author ash
 */
public class Config {

  private boolean isCompatibleWithEmptyList = true;

  private boolean isCheckColumn = false;

  private boolean isUseActualParamName = false;

  private boolean isUseTransactionForBatchUpdate = false;

  public boolean isCompatibleWithEmptyList() {
    return isCompatibleWithEmptyList;
  }

  public void setCompatibleWithEmptyList(boolean compatibleWithEmptyList) {
    isCompatibleWithEmptyList = compatibleWithEmptyList;
  }

  public boolean isCheckColumn() {
    return isCheckColumn;
  }

  public void setCheckColumn(boolean checkColumn) {
    isCheckColumn = checkColumn;
  }

  public boolean isUseActualParamName() {
    return isUseActualParamName;
  }

  public void setUseActualParamName(boolean useActualParamName) {
    isUseActualParamName = useActualParamName;
  }

  public boolean isUseTransactionForBatchUpdate() {
    return isUseTransactionForBatchUpdate;
  }

  public void setUseTransactionForBatchUpdate(boolean useTransactionForBatchUpdate) {
    isUseTransactionForBatchUpdate = useTransactionForBatchUpdate;
  }

  public Config copy() {
    Config config = new Config();
    config.setCompatibleWithEmptyList(isCompatibleWithEmptyList());
    config.setCheckColumn(isCheckColumn());
    config.setUseActualParamName(isUseActualParamName());
    config.setUseTransactionForBatchUpdate(isUseTransactionForBatchUpdate());
    return config;
  }
}
