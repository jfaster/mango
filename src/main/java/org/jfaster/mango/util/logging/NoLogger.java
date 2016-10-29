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

package org.jfaster.mango.util.logging;

/**
 * @author ash
 */
public class NoLogger extends AbstractInternalLogger {

  NoLogger(String name) {
    super(name);
  }

  @Override
  public boolean isTraceEnabled() {
    return false;
  }

  @Override
  public void trace(String msg) {
  }

  @Override
  public void trace(String format, Object arg) {
  }

  @Override
  public void trace(String format, Object argA, Object argB) {
  }

  @Override
  public void trace(String format, Object... arguments) {
  }

  @Override
  public void trace(String msg, Throwable t) {
  }

  @Override
  public boolean isDebugEnabled() {
    return false;
  }

  @Override
  public void debug(String msg) {
  }

  @Override
  public void debug(String format, Object arg) {
  }

  @Override
  public void debug(String format, Object argA, Object argB) {
  }

  @Override
  public void debug(String format, Object... arguments) {
  }

  @Override
  public void debug(String msg, Throwable t) {
  }

  @Override
  public boolean isInfoEnabled() {
    return false;
  }

  @Override
  public void info(String msg) {
  }

  @Override
  public void info(String format, Object arg) {
  }

  @Override
  public void info(String format, Object argA, Object argB) {
  }

  @Override
  public void info(String format, Object... arguments) {
  }

  @Override
  public void info(String msg, Throwable t) {
  }

  @Override
  public boolean isWarnEnabled() {
    return false;
  }

  @Override
  public void warn(String msg) {
  }

  @Override
  public void warn(String format, Object arg) {
  }

  @Override
  public void warn(String format, Object... arguments) {
  }

  @Override
  public void warn(String format, Object argA, Object argB) {
  }

  @Override
  public void warn(String msg, Throwable t) {
  }

  @Override
  public boolean isErrorEnabled() {
    return false;
  }

  @Override
  public void error(String msg) {
  }

  @Override
  public void error(String format, Object arg) {
  }

  @Override
  public void error(String format, Object argA, Object argB) {
  }

  @Override
  public void error(String format, Object... arguments) {
  }

  @Override
  public void error(String msg, Throwable t) {
  }
}
