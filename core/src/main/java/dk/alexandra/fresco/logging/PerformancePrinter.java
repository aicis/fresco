package dk.alexandra.fresco.logging;

public interface PerformancePrinter {
  
  /**
   * Prints any performance numbers picked up by the 
   * given PerformanceLogger.
   */
  public void printPerformanceLog(PerformanceLogger logger);
}
