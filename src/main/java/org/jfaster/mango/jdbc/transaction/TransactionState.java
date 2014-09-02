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

package org.jfaster.mango.jdbc.transaction;

/**
 * 事务状态
 *
 * @author ash
 */
public enum TransactionState {

    /**
     * 事务运行中
     */
    RUNNING,

    /**
     * 提交事务成功
     */
    COMMIT_SUCCESS,

    /**
     * 提交事务失败
     */
    COMMIT_FAIL,

    /**
     * 回滚事务成功
     */
    ROLLBACK_SUCCESS,

    /**
     * 回滚事务失败
     */
    ROLLBACK_FAIL;


}
