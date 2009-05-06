/*******************************************************************************
 * Copyright (c) 2007 Vlad Dumitrescu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vlad Dumitrescu
 *******************************************************************************/
package org.erlide.runtime;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;

public class ErlLogger {

	private static final String ERLIDE_GLOBAL_TRACE_OPTION = "org.erlide.launching/debug";
	private static int minLevel = Level.FINEST.intValue();

	{
		// This is not run?!?
		final String lvl = System.getProperty("erlide.logger.level");
		minLevel = (lvl == null ? Level.INFO : Level.parse(lvl.toUpperCase()))
				.intValue();
	}

	private static StackTraceElement getCaller() {
		final StackTraceElement[] st = Thread.currentThread().getStackTrace();
		StackTraceElement el = null;
		int i = 2;
		do {
			el = st[i++];
		} while (el.getClassName().equals(ErlLogger.class.getName()));
		return el;
	}

	private static void log(final Level kind, final String fmt,
			final Object... o) {
		if (kind.intValue() < minLevel) {
			return;
		}
		final StackTraceElement el = getCaller();
		final String str = o.length == 0 ? fmt : String.format(fmt, o);
		Logger.getLogger("org.erlide").logp(
				kind,
				el.getClassName(),
				el.getMethodName(),
				"(" + el.getFileName() + ":" + el.getLineNumber() + ") : "
						+ str);
	}

	private static void log(final Level kind, final Exception e) {
		if (kind.intValue() < minLevel) {
			return;
		}
		final StackTraceElement el = getCaller();
		final String str = e.getMessage();

		Logger.getLogger("org.erlide").logp(
				kind,
				el.getClassName(),
				el.getMethodName(),
				"(" + el.getFileName() + ":" + el.getLineNumber() + ") : "
						+ str, e);
	}

	public static void erlangLog(final String module, final int line,
			final String skind, final String fmt, final Object... o) {
		final Level kind = Level.parse(skind);
		if (kind.intValue() < minLevel) {
			return;
		}
		final String str = o.length == 0 ? fmt : String.format(fmt, o);
		Logger.getLogger("org.erlide").log(kind,
				"(" + module + ":" + line + ") : " + str);
	}

	public static void debug(final String fmt, final Object... o) {
		log(Level.FINEST, fmt, o);
	}

	public static void info(final String fmt, final Object... o) {
		log(Level.INFO, fmt, o);
	}

	public static void warn(final String fmt, final Object... o) {
		log(Level.WARNING, fmt, o);
	}

	public static void error(final String fmt, final Object... o) {
		log(Level.SEVERE, fmt, o);
	}

	public static void debug(final Exception e) {
		log(Level.FINEST, e);
	}

	public static void info(final Exception e) {
		log(Level.INFO, e);
	}

	public static void warn(final Exception e) {
		log(Level.WARNING, e);
	}

	public static void error(final Exception e) {
		log(Level.SEVERE, e);
	}

	public static class ErlSimpleFormatter extends Formatter {

		Date dat = new Date();
		private static final String format = "{0,time,HH:mm:ss,SSS}";
		private MessageFormat formatter;

		private final Object[] args = new Object[1];

		private final String lineSeparator = System
				.getProperty("line.separator");

		@Override
		public synchronized String format(final LogRecord record) {
			final StringBuffer sb = new StringBuffer();
			// Minimize memory allocations here.
			dat.setTime(record.getMillis());
			args[0] = dat;
			final StringBuffer text = new StringBuffer();
			if (formatter == null) {
				formatter = new MessageFormat(format);
			}
			formatter.format(args, text, null);
			sb.append(text);
			sb.append(" ");
			final String message = formatMessage(record);
			sb.append(record.getLevel().toString().charAt(0));
			sb.append(": ");
			sb.append(message);
			sb.append(lineSeparator);
			if (record.getThrown() != null) {
				try {
					final StringWriter sw = new StringWriter();
					final PrintWriter pw = new PrintWriter(sw);
					record.getThrown().printStackTrace(pw);
					pw.close();
					sb.append(sw.toString());
				} catch (final Exception ex) {
				}
			}
			return sb.toString();
		}
	}

	public static void trace(final String traceOption, final String fmt,
			final Object... args) {
		if (!Platform.inDebugMode()) {
			return;
		}
		final String globalTraceValue = Platform
				.getDebugOption(ERLIDE_GLOBAL_TRACE_OPTION);
		final String value = Platform.getDebugOption(ERLIDE_GLOBAL_TRACE_OPTION
				+ "/" + traceOption);
		if (null != globalTraceValue && globalTraceValue.equals("true")
				&& null != value && value.equals("true")) {
			debug(fmt, args);
		}
	}

	public static void init() {
		Handler fh;
		try {
			final ErlSimpleFormatter erlSimpleFormatter = new ErlSimpleFormatter();
			final Logger logger = Logger.getLogger("org.erlide");

			String dir = ResourcesPlugin.getWorkspace().getRoot().getLocation()
					.toPortableString();
			dir = dir == null ? "c:/" : dir;
			fh = new FileHandler(dir + "_erlide.log");
			fh.setFormatter(erlSimpleFormatter);
			fh.setLevel(java.util.logging.Level.FINEST);
			logger.addHandler(fh);

			final ConsoleHandler consoleHandler = new ConsoleHandler();
			consoleHandler.setFormatter(erlSimpleFormatter);
			final Level lvl = Platform.inDebugMode() ? java.util.logging.Level.FINEST
					: java.util.logging.Level.SEVERE;
			consoleHandler.setLevel(lvl);
			logger.addHandler(consoleHandler);

			logger.setUseParentHandlers(false);
			logger.setLevel(java.util.logging.Level.FINEST);
		} catch (final SecurityException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
