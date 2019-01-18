package dk.alexandra.fresco.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatrixIterationLogPrinter {

  private static Logger log = LoggerFactory.getLogger(MatrixIterationLogPrinter.class);
  private static final String MATRIX_LOG_DIR = "MATRIX/logs/";
  private static final String MATRIX_LOG_EXTENTION = ".log";
  private final String experimentName;
  private final int numberOfParties;

  public MatrixIterationLogPrinter(String experimentName, int numberOfParties) {
    this.experimentName = experimentName;
    this.numberOfParties = numberOfParties;
  }

  public void printPerformanceLog(List<PerformanceLogger> loggers) {
    if (loggers.isEmpty()) {
      log.warn("List of loggers is empty. Nothing to print");
      return;
    }
    String fileName = MATRIX_LOG_DIR + experimentName + MATRIX_LOG_EXTENTION;
    (new File(MATRIX_LOG_DIR)).mkdirs();
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
      writer.write(numberOfParties + "\n");
      PerformanceLogger firstLog = loggers.get(0);
      for (String k: firstLog.getLoggedValues().keySet()) {
        writer.write(k + ":");
        for (PerformanceLogger logger : loggers) {
          Long value = logger.getLoggedValues().get(k);
          String valString = value == null ? "N/A" : value.toString();
          writer.write(valString + ", ");
        }
        writer.write("\n");
      }
    } catch (IOException ex) {
      log.error("Unable to write log to " + fileName, ex);
    }
  }

}
