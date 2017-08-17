package com.github.bustedearlobes.themis.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * This class is used to format the log entries.
 */
public class ThemisLogFormatter extends Formatter {
	private static final DateFormat df = new SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ss.SSS");
	
	@Override
	public String format(LogRecord record) {
		StringBuilder builder = new StringBuilder(750);
        builder.append(df.format(new Date(record.getMillis()))).append(" ");
        builder.append("[").append(record.getLevel()).append("] ");
        builder.append(record.getSourceClassName()
        		.replaceFirst("com.github.bustedearlobes.themis.", ""))
        		.append(".");
        builder.append(record.getSourceMethodName()).append(": ");
        builder.append(formatMessage(record));
        builder.append("\n");
        return builder.toString();
	}
	
	
}
