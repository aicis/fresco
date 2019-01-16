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
  private static final String MATRIX_LOG_DIR = "MATRIX/logs/";
  private static final String MATRIX_LOG_EXTENTION = ".log";
  private final String experimentName;
  private final int numberOfParties;

  public MatrixLogPrinter(String experimentName, int numberOfParties) {
    this.experimentName = experimentName;
    this.numberOfParties = numberOfParties;
  }

  @Override
  public void printPerformanceLog(PerformanceLogger logger) {
    String fileName = MATRIX_LOG_DIR + experimentName + MATRIX_LOG_EXTENTION;
    (new File(MATRIX_LOG_DIR)).mkdirs();
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
      writer.write(numberOfParties + "\n");
      for (Entry<String, Long> e: logger.getLoggedValues().entrySet()) {
        writer.write(e.getKey() + ":" + e.getValue() + "," + "\n");
      }
    } catch (IOException ex) {
      log.error("Unable to write log to " + fileName, ex);
    }
  }

}
