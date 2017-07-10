package dk.alexandra.fresco.framework;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class TestReporter {

  
  private Logger logger = Logger.getLogger(Reporter.class.getName());
  private String newline = System.getProperty("line.separator");
  private TestHandler handler = new TestHandler();
  private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
  private CustomFormatter formatter = new CustomFormatter();

  @Before
  public void setUp() throws Exception {
    Reporter.init(Level.ALL);
    logger.addHandler(handler);
  }

  @Test
  public void testConstructor() {
    Reporter reporter = new Reporter();
    Assert.assertNotNull(reporter);
  }
  
  @Test
  public void testInfo() {
    Reporter.info("INFO MESSAGE");
    Assert.assertThat(handler.getLastMessage(), Is.is("INFO MESSAGE"));
  }
  
  @Test
  public void testWarn() {
    Reporter.warn("WARN MESSAGE");
    Assert.assertThat(handler.getLastMessage(), Is.is("WARN MESSAGE"));
  }

  @Test
  public void testFine() {
    Reporter.fine("FINE MESSAGE");
    Assert.assertThat(handler.getLastMessage(), Is.is("FINE MESSAGE"));
  }
  
  @Test
  public void testFiner() {
    Reporter.finer("FINER MESSAGE");
    Assert.assertThat(handler.getLastMessage(), Is.is("FINER MESSAGE"));
  }
  
  @Test
  public void testFinest() {
    Reporter.finest("FINEST MESSAGE");
    Assert.assertThat(handler.getLastMessage(), Is.is("FINEST MESSAGE"));
  }

  @Test
  public void testConfig() {
    Reporter.config("CONFIG MESSAGE");
    Assert.assertThat(handler.getLastMessage(), Is.is("CONFIG MESSAGE"));
  }
  
  @Test
  public void testSevere() {
    Reporter.severe("SEVERE MESSAGE");
    Assert.assertThat(handler.getLastMessage(), Is.is("SEVERE MESSAGE"));
  }
  
  @Test
  public void testSevereWithException() {
    Exception ex = new Exception("Exception");
    Reporter.severe("SEVERE MESSAGE", ex);
    String expectedString = "SEVERE MESSAGE" + newline + getStackTrace(ex);
    Assert.assertThat(handler.getLastMessage(), Is.is(expectedString));
  }
  
  @Test
  public void testFormatterNullMessage() {
     LogRecord record = new LogRecord(Level.INFO, null);
     Date date = new Date(record.getMillis());
     String out = formatter.format(record);
     String expected = "["+dateFormat.format(date)+"] ("+record.getThreadID()+") INFO: null\n";
     Assert.assertThat(out, Is.is(expected));
  }
  
  @Test
  public void testFormatterEmptyMessage() {
     LogRecord record = new LogRecord(Level.INFO, "");
     Date date = new Date(record.getMillis());
     String formattedOutput = formatter.format(record);
     String expected = "["+dateFormat.format(date)+"] ("+record.getThreadID()+") INFO: \n";
     Assert.assertThat(formattedOutput, Is.is(expected));
  }
  
  @Ignore //TODO
  @Test
  public void testFormatterEmptyMessageException() {
     LogRecord record = new LogRecord(Level.INFO, "");
     record.setMessage(null);
     Date date = new Date(record.getMillis());
     Exception thrown = new Exception("Exception");
     record.setThrown(thrown);
     String out = formatter.format(record);
     String expected = "["+dateFormat.format(date)+"] ("+record.getThreadID()+") INFO: " + thrown.getMessage()+"\n";
     Assert.assertThat(out, Is.is(expected));
  }

  @Test
  public void testFormatterNullSourceMethod() {
     LogRecord record = new LogRecord(Level.INFO, "Message");
     Date date = new Date(record.getMillis());
     record.setSourceMethodName(null);
     String out = formatter.format(record);
     String expected = "["+dateFormat.format(date)+"] ("+record.getThreadID()+") INFO: Message\n";
     Assert.assertThat(out, Is.is(expected));
  }
  
  @Test
  public void testFormatterSourceClassMethod() {
     LogRecord record = new LogRecord(Level.INFO, "Message");
     Date date = new Date(record.getMillis());
     record.setSourceClassName("myclass.");
     String out = formatter.format(record);
     String expected = "["+dateFormat.format(date)+"] ("+record.getThreadID()+") INFO: Message\n";
     Assert.assertThat(out, Is.is(expected));
  }
  
  /* TODO
  @Test
  public void testCustomConstructorFormat() {
    CustomFormatter customFormatter = new CustomFormatter("%m");
    LogRecord record = new LogRecord(Level.INFO, "Message");
    String out = customFormatter.format(record);
    String expected = "Message\n";
    Assert.assertThat(out, Is.is(expected));
  }
  
  @Test
  public void testCustomConstructorFormatNull() {
    CustomFormatter customFormatter = new CustomFormatter(null);
    LogRecord record = new LogRecord(Level.INFO, "Message");
    Date date = new Date(record.getMillis());
    String out = customFormatter.format(record);
    String expected = "["+dateFormat.format(date)+"] ("+record.getThreadID()+") INFO: Message\n";
    Assert.assertThat(out, Is.is(expected));
  }
  
  @Test
  public void testCustomConstructorFormatEmpty() {
    CustomFormatter customFormatter = new CustomFormatter("");
    LogRecord record = new LogRecord(Level.INFO, "Message");
    Date date = new Date(record.getMillis());
    String out = customFormatter.format(record);
    String expected = "["+dateFormat.format(date)+"] ("+record.getThreadID()+") INFO: Message\n";
    Assert.assertThat(out, Is.is(expected));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testCustomConstructorFormatCurly() {
    CustomFormatter customFormatter = new CustomFormatter("something {");
    LogRecord record = new LogRecord(Level.INFO, "Message");
    customFormatter.format(record);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testCustomConstructorFormatCurly2() {
    CustomFormatter customFormatter = new CustomFormatter("something }");
    LogRecord record = new LogRecord(Level.INFO, "Message");
    customFormatter.format(record);
  }*/
  
  private class TestHandler extends Handler {

    private String message = null;
    
    public String getLastMessage() {
      return message;
    }
    
    @Override
    public void publish(LogRecord record) {
      message = record.getMessage();
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
    
  }
 
  /**
   * Duplicate code from Reporter. Should be changed in the future TODO
   * @param ex
   * @return
   */
  private static String getStackTrace(Throwable ex) {
    Writer result = new StringWriter();
    PrintWriter printWriter = new PrintWriter(result);
    ex.printStackTrace(printWriter);
    return result.toString();
}
  
}
