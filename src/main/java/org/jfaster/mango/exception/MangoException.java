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

package org.jfaster.mango.exception;

/**
 * 所有的runtime异常继承此类
 *
 * @author ash
 */
public abstract class MangoException extends RuntimeException {

  public MangoException(String msg) {
    super(msg);
  }

  public MangoException(String msg, Throwable cause) {
    super(msg, cause);
  }

  @Override
  public String getMessage() {
    return buildMessage(super.getMessage(), getCause());
  }

  private String buildMessage(String message, Throwable cause) {
    if (cause != null) {
      StringBuilder sb = new StringBuilder();
      if (message != null) {
        sb.append(message).append("; ");
      }
      sb.append("caused by: ").append(cause.getMessage());
      return sb.toString();
    } else {
      return message;
    }
  }

}
