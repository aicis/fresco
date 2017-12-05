package dk.alexandra.fresco.logging;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * Ultra simple Logger implementation for test purposes.
 * Logged statements are appended to a list which can be retrieved
 * using getData.
 * Currently only info(String) is supported. 
 *
 */
public class ListLogger implements Logger{

  private List<String> data = new ArrayList<>();

  public List<String> getData() {
    return data;
  }
  
  @Override
  public void info(String arg0) {
    data.add(arg0);
  }
  
  @Override
  public void debug(String arg0) {
  }

  @Override
  public void debug(String arg0, Object arg1) {
  }

  @Override
  public void debug(String arg0, Object... arg1) {
  }

  @Override
  public void debug(String arg0, Throwable arg1) {
  }

  @Override
  public void debug(Marker arg0, String arg1) {
  }

  @Override
  public void debug(String arg0, Object arg1, Object arg2) {
  }

  @Override
  public void debug(Marker arg0, String arg1, Object arg2) {
  }

  @Override
  public void debug(Marker arg0, String arg1, Object... arg2) {
  }

  @Override
  public void debug(Marker arg0, String arg1, Throwable arg2) {
  }

  @Override
  public void debug(Marker arg0, String arg1, Object arg2, Object arg3) {
  }

  @Override
  public void error(String arg0) {
  }

  @Override
  public void error(String arg0, Object arg1) {
  }

  @Override
  public void error(String arg0, Object... arg1) {
  }

  @Override
  public void error(String arg0, Throwable arg1) {
  }

  @Override
  public void error(Marker arg0, String arg1) {
  }

  @Override
  public void error(String arg0, Object arg1, Object arg2) {
  }

  @Override
  public void error(Marker arg0, String arg1, Object arg2) {
  }

  @Override
  public void error(Marker arg0, String arg1, Object... arg2) {
  }

  @Override
  public void error(Marker arg0, String arg1, Throwable arg2) {
  }

  @Override
  public void error(Marker arg0, String arg1, Object arg2, Object arg3) {
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public void info(String arg0, Object arg1) {
  }

  @Override
  public void info(String arg0, Object... arg1) {
  }

  @Override
  public void info(String arg0, Throwable arg1) {
  }

  @Override
  public void info(Marker arg0, String arg1) {
  }

  @Override
  public void info(String arg0, Object arg1, Object arg2) {
  }

  @Override
  public void info(Marker arg0, String arg1, Object arg2) {
  }

  @Override
  public void info(Marker arg0, String arg1, Object... arg2) {
  }

  @Override
  public void info(Marker arg0, String arg1, Throwable arg2) {
  }

  @Override
  public void info(Marker arg0, String arg1, Object arg2, Object arg3) {
  }

  @Override
  public boolean isDebugEnabled() {
    return false;
  }

  @Override
  public boolean isDebugEnabled(Marker arg0) {
    return false;
  }

  @Override
  public boolean isErrorEnabled() {
    return false;
  }

  @Override
  public boolean isErrorEnabled(Marker arg0) {
    return false;
  }

  @Override
  public boolean isInfoEnabled() {
    return false;
  }

  @Override
  public boolean isInfoEnabled(Marker arg0) {
    return false;
  }

  @Override
  public boolean isTraceEnabled() {
    return false;
  }

  @Override
  public boolean isTraceEnabled(Marker arg0) {
    return false;
  }

  @Override
  public boolean isWarnEnabled() {
    return false;
  }

  @Override
  public boolean isWarnEnabled(Marker arg0) {
    return false;
  }

  @Override
  public void trace(String arg0) {
  }

  @Override
  public void trace(String arg0, Object arg1) {
  }

  @Override
  public void trace(String arg0, Object... arg1) {
  }

  @Override
  public void trace(String arg0, Throwable arg1) {
  }

  @Override
  public void trace(Marker arg0, String arg1) {
  }

  @Override
  public void trace(String arg0, Object arg1, Object arg2) {
  }

  @Override
  public void trace(Marker arg0, String arg1, Object arg2) {
  }

  @Override
  public void trace(Marker arg0, String arg1, Object... arg2) {
  }

  @Override
  public void trace(Marker arg0, String arg1, Throwable arg2) {
  }

  @Override
  public void trace(Marker arg0, String arg1, Object arg2, Object arg3) {
  }

  @Override
  public void warn(String arg0) {
  }

  @Override
  public void warn(String arg0, Object arg1) {
  }

  @Override
  public void warn(String arg0, Object... arg1) {
  }

  @Override
  public void warn(String arg0, Throwable arg1) {
  }

  @Override
  public void warn(Marker arg0, String arg1) {
  }

  @Override
  public void warn(String arg0, Object arg1, Object arg2) {
  }

  @Override
  public void warn(Marker arg0, String arg1, Object arg2) {
  }

  @Override
  public void warn(Marker arg0, String arg1, Object... arg2) {
  }

  @Override
  public void warn(Marker arg0, String arg1, Throwable arg2) {
  }

  @Override
  public void warn(Marker arg0, String arg1, Object arg2, Object arg3) {
  }
}