/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.framework;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Reporter {
	
	private static Logger logger;
	
	private static String newline = System.getProperty("line.separator");
	
	public synchronized static void init(Level logLevel) {
		
		if (logger != null) {
			logger.fine("Reporter already initialized");
			return;
		}
		
		logger = Logger.getLogger(Reporter.class.getName());
		logger.setLevel(Level.ALL);
		
		Handler handler = new ConsoleHandler();
		handler.setLevel(logLevel);
		
		// We need CustomFormatter to easily log on single line
		handler.setFormatter(new CustomFormatter());
		
		logger.addHandler(handler);
		
		// Set this to false in order to turn of logging to stderr.
		logger.setUseParentHandlers(false);
	}

	public synchronized static void info(String msg) {
		logger.info(msg);
	}
	
	public synchronized static void warn(String msg) {
		logger.warning(msg);
	}

	public synchronized static void fine(String msg) {
		logger.fine(msg);
	}

	public synchronized static void finer(String msg) {
		logger.finer(msg);
	}
	
	public synchronized static void finest(String msg) {
		logger.finest(msg);
	}

	public synchronized static void config(String msg) {
		logger.config(msg);
	}
	

	public synchronized static void severe(String msg) {
			logger.severe(msg);
	}
	
	public synchronized static void severe(String msg, Throwable ex) {
		
		severe(msg + newline + getStackTrace(ex));
		
		// TODO: How to get exception shown here? Probably our
		// custom handler doesn't support it. Consider using
		// log4j rather than util.logging.
		//logger.log(Level.SEVERE, msg, ex);
	}

	private static String getStackTrace(Throwable ex) {
	    final Writer result = new StringWriter();
	    final PrintWriter printWriter = new PrintWriter(result);
	    ex.printStackTrace(printWriter);
	    return result.toString();
	}

	
}


/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
/**
 * A {@link Formatter} that may be customised in a {@code logging.properties}
 * file. The syntax of the property
 * {@code com.thinktankmaths.logging.TerseFormatter.format}
 * specifies the output. A newline will be appended to the string and the
 * following special characters will be expanded (case sensitive):-
 * <ul>
 * <li>{@code %m} - message</li>
 * <li>{@code %L} - log level</li>
 * <li>{@code %n} - name of the logger</li>
 * <li>{@code %t} - a timestamp (in ISO-8601 "yyyy-MM-dd HH:mm:ss Z" format)</li>
 * <li>{@code %M} - source method name (if available, otherwise "?")</li>
 * <li>{@code %c} - source class name (if available, otherwise "?")</li>
 * <li>{@code %C} - source simple class name (if available, otherwise "?")</li>
 * <li>{@code %T} - thread ID</li>
 * </ul>
 * The default format is {@value #DEFAULT_FORMAT}. Curly brace characters are not
 * allowed.
 * 
 * @author Samuel Halliday
 * 
 */
class CustomFormatter extends Formatter {
    private static final String DEFAULT_FORMAT = "[%t] (%T) %L: %m";
 
    private final MessageFormat messageFormat;
 
    private final DateFormat dateFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
 
    /** */
    public CustomFormatter() {
        super();
 
        // load the format from logging.properties
        String propName = getClass().getName() + ".format";
        String format = LogManager.getLogManager().getProperty(propName);
        if (format == null || format.trim().length() == 0)
            format = DEFAULT_FORMAT;
        if (format.contains("{") || format.contains("}"))
            throw new IllegalArgumentException("curly braces not allowed");
 
        // convert it into the MessageFormat form
        format = format.replace("%L", "{0}").replace("%m", "{1}").replace("%M",
            "{2}").replace("%t", "{3}").replace("%c", "{4}").replace("%T", "{5}").
            replace("%n", "{6}").replace("%C", "{7}") + "\n";
 
        messageFormat = new MessageFormat(format);
    }
 
    @Override
    public String format(LogRecord record) {
        String[] arguments = new String[8];
        // %L
        arguments[0] = record.getLevel().toString();
        arguments[1] = record.getMessage();
        // sometimes the message is empty, but there is a throwable
        if (arguments[1] == null || arguments[1].length() == 0) {
            Throwable thrown = record.getThrown();
            if (thrown != null) {
                arguments[1] = thrown.getMessage();
            }
        }
        // %m
        arguments[1] = record.getMessage();
        // %M
        if (record.getSourceMethodName() != null) {
            arguments[2] = record.getSourceMethodName();
        } else {
            arguments[2] = "?";
        }
        // %t
        Date date = new Date(record.getMillis());
        synchronized (dateFormat) {
            arguments[3] = dateFormat.format(date);
        }
        // %c
        if (record.getSourceClassName() != null) {
            arguments[4] = record.getSourceClassName();
        } else {
            arguments[4] = "?";
        }
        // %T
        arguments[5] = Integer.valueOf(record.getThreadID()).toString();
        // %n
        arguments[6] = record.getLoggerName();
        // %C
        int start = arguments[4].lastIndexOf(".") + 1;
        if (start > 0 && start < arguments[4].length()) {
            arguments[7] = arguments[4].substring(start);
        } else {
            arguments[7] = arguments[4];
        }
 
        synchronized (messageFormat) {
            return messageFormat.format(arguments);
        }
    }
}
