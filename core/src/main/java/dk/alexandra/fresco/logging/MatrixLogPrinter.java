package dk.alexandra.fresco.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatrixLogPrinter implements PerformancePrinter {

  private static Logger log = LoggerFactory.getLogger(MatrixLogPrinter.class);
  private File logFile;

  public MatrixLogPrinter(String experimentName) {
    this(new File(experimentName + ".log"));
  }

  public MatrixLogPrinter(File logFile) {
    this.logFile = logFile;
  }

  @Override
  public void printPerformanceLog(PerformanceLogger logger) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {
      for (Entry<String, Long> e: logger.getLoggedValues().entrySet()) {
        writer.write(e.getKey() + ":" + e.getValue() + "," + "\n");
      }
    } catch (IOException ex) {
      log.error("Unable to write log to " + logFile.getName(), ex);
    }
  }

}
