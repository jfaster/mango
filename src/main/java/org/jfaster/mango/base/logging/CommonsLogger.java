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

package org.jfaster.mango.base.logging;

import org.apache.commons.logging.Log;

/**
 * <a href="http://commons.apache.org/logging/">Apache Commons Logging</a>
 * logger.
 */
class CommonsLogger extends AbstractInternalLogger {

    private static final long serialVersionUID = 8647838678388394885L;

    private final transient Log logger;

    CommonsLogger(Log logger, String name) {
        super(name);
        if (logger == null) {
            throw new NullPointerException("logger");
        }
        this.logger = logger;
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#isTraceEnabled} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     */
    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#trace(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * @param msg - the message object to be logged
     */
    @Override
    public void trace(String msg) {
        logger.trace(msg);
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#trace(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * <p>
     * However, this form avoids superfluous object creation when the logger is disabled
     * for level TRACE.
     * </p>
     *
     * @param format
     *          the format string
     * @param arg
     *          the argument
     */
    @Override
    public void trace(String format, Object arg) {
        if (logger.isTraceEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            logger.trace(ft.getMessage(), ft.getThrowable());
        }
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#trace(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * <p>
     * However, this form avoids superfluous object creation when the logger is disabled
     * for level TRACE.
     * </p>
     *
     * @param format
     *          the format string
     * @param argA
     *          the first argument
     * @param argB
     *          the second argument
     */
    @Override
    public void trace(String format, Object argA, Object argB) {
        if (logger.isTraceEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, argA, argB);
            logger.trace(ft.getMessage(), ft.getThrowable());
        }
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#trace(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * <p>
     * However, this form avoids superfluous object creation when the logger is disabled
     * for level TRACE.
     * </p>
     *
     * @param format the format string
     * @param arguments a list of 3 or more arguments
     */
    @Override
    public void trace(String format, Object... arguments) {
        if (logger.isTraceEnabled()) {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
            logger.trace(ft.getMessage(), ft.getThrowable());
        }
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#trace(Object, Throwable)} method of
     * the underlying {@link org.apache.commons.logging.Log} instance.
     *
     * @param msg
     *          the message accompanying the exception
     * @param t
     *          the exception (throwable) to log
     */
    @Override
    public void trace(String msg, Throwable t) {
        logger.trace(msg, t);
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#isDebugEnabled} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     */
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    //

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#debug(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * @param msg - the message object to be logged
     */
    @Override
    public void debug(String msg) {
        logger.debug(msg);
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#debug(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * <p>
     * However, this form avoids superfluous object creation when the logger is disabled
     * for level DEBUG.
     * </p>
     *
     * @param format
     *          the format string
     * @param arg
     *          the argument
     */
    @Override
    public void debug(String format, Object arg) {
        if (logger.isDebugEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            logger.debug(ft.getMessage(), ft.getThrowable());
        }
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#debug(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * <p>
     * However, this form avoids superfluous object creation when the logger is disabled
     * for level DEBUG.
     * </p>
     *
     * @param format
     *          the format string
     * @param argA
     *          the first argument
     * @param argB
     *          the second argument
     */
    @Override
    public void debug(String format, Object argA, Object argB) {
        if (logger.isDebugEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, argA, argB);
            logger.debug(ft.getMessage(), ft.getThrowable());
        }
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#debug(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * <p>
     * However, this form avoids superfluous object creation when the logger is disabled
     * for level DEBUG.
     * </p>
     *
     * @param format the format string
     * @param arguments a list of 3 or more arguments
     */
    @Override
    public void debug(String format, Object... arguments) {
        if (logger.isDebugEnabled()) {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
            logger.debug(ft.getMessage(), ft.getThrowable());
        }
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#debug(Object, Throwable)} method of
     * the underlying {@link org.apache.commons.logging.Log} instance.
     *
     * @param msg
     *          the message accompanying the exception
     * @param t
     *          the exception (throwable) to log
     */
    @Override
    public void debug(String msg, Throwable t) {
        logger.debug(msg, t);
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#isInfoEnabled} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     */
    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#debug(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * @param msg - the message object to be logged
     */
    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#info(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * <p>
     * However, this form avoids superfluous object creation when the logger is disabled
     * for level INFO.
     * </p>
     *
     * @param format
     *          the format string
     * @param arg
     *          the argument
     */

    @Override
    public void info(String format, Object arg) {
        if (logger.isInfoEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            logger.info(ft.getMessage(), ft.getThrowable());
        }
    }
    /**
     * Delegates to the {@link org.apache.commons.logging.Log#info(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * <p>
     * However, this form avoids superfluous object creation when the logger is disabled
     * for level INFO.
     * </p>
     *
     * @param format
     *          the format string
     * @param argA
     *          the first argument
     * @param argB
     *          the second argument
     */
    @Override
    public void info(String format, Object argA, Object argB) {
        if (logger.isInfoEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, argA, argB);
            logger.info(ft.getMessage(), ft.getThrowable());
        }
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#info(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * <p>
     * However, this form avoids superfluous object creation when the logger is disabled
     * for level INFO.
     * </p>
     *
     * @param format the format string
     * @param arguments a list of 3 or more arguments
     */
    @Override
    public void info(String format, Object... arguments) {
        if (logger.isInfoEnabled()) {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
            logger.info(ft.getMessage(), ft.getThrowable());
        }
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#info(Object, Throwable)} method of
     * the underlying {@link org.apache.commons.logging.Log} instance.
     *
     * @param msg
     *          the message accompanying the exception
     * @param t
     *          the exception (throwable) to log
     */
    @Override
    public void info(String msg, Throwable t) {
        logger.info(msg, t);
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#isWarnEnabled} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     */
    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#warn(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * @param msg - the message object to be logged
     */
    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#warn(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * <p>
     * However, this form avoids superfluous object creation when the logger is disabled
     * for level WARN.
     * </p>
     *
     * @param format
     *          the format string
     * @param arg
     *          the argument
     */
    @Override
    public void warn(String format, Object arg) {
        if (logger.isWarnEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            logger.warn(ft.getMessage(), ft.getThrowable());
        }
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#warn(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * <p>
     * However, this form avoids superfluous object creation when the logger is disabled
     * for level WARN.
     * </p>
     *
     * @param format
     *          the format string
     * @param argA
     *          the first argument
     * @param argB
     *          the second argument
     */
    @Override
    public void warn(String format, Object argA, Object argB) {
        if (logger.isWarnEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, argA, argB);
            logger.warn(ft.getMessage(), ft.getThrowable());
        }
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#warn(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * <p>
     * However, this form avoids superfluous object creation when the logger is disabled
     * for level WARN.
     * </p>
     *
     * @param format the format string
     * @param arguments a list of 3 or more arguments
     */
    @Override
    public void warn(String format, Object... arguments) {
        if (logger.isWarnEnabled()) {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
            logger.warn(ft.getMessage(), ft.getThrowable());
        }
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#warn(Object, Throwable)} method of
     * the underlying {@link org.apache.commons.logging.Log} instance.
     *
     * @param msg
     *          the message accompanying the exception
     * @param t
     *          the exception (throwable) to log
     */

    @Override
    public void warn(String msg, Throwable t) {
        logger.warn(msg, t);
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#isErrorEnabled} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     */
    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#error(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * @param msg - the message object to be logged
     */
    @Override
    public void error(String msg) {
        logger.error(msg);
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#error(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * <p>
     * However, this form avoids superfluous object creation when the logger is disabled
     * for level ERROR.
     * </p>
     *
     * @param format
     *          the format string
     * @param arg
     *          the argument
     */
    @Override
    public void error(String format, Object arg) {
        if (logger.isErrorEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            logger.error(ft.getMessage(), ft.getThrowable());
        }
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#error(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * <p>
     * However, this form avoids superfluous object creation when the logger is disabled
     * for level ERROR.
     * </p>
     *
     * @param format
     *          the format string
     * @param argA
     *          the first argument
     * @param argB
     *          the second argument
     */
    @Override
    public void error(String format, Object argA, Object argB) {
        if (logger.isErrorEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, argA, argB);
            logger.error(ft.getMessage(), ft.getThrowable());
        }
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#error(Object)} method of the underlying
     * {@link org.apache.commons.logging.Log} instance.
     *
     * <p>
     * However, this form avoids superfluous object creation when the logger is disabled
     * for level ERROR.
     * </p>
     *
     * @param format the format string
     * @param arguments a list of 3 or more arguments
     */
    @Override
    public void error(String format, Object... arguments) {
        if (logger.isErrorEnabled()) {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
            logger.error(ft.getMessage(), ft.getThrowable());
        }
    }

    /**
     * Delegates to the {@link org.apache.commons.logging.Log#error(Object, Throwable)} method of
     * the underlying {@link org.apache.commons.logging.Log} instance.
     *
     * @param msg
     *          the message accompanying the exception
     * @param t
     *          the exception (throwable) to log
     */
    @Override
    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }
}
