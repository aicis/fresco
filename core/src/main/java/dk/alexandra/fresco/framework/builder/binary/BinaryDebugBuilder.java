package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SBool;
import java.io.PrintStream;
import java.util.List;

public interface BinaryDebugBuilder {

  /**
   * When evaluated, opens the given SBools and prints them to System.out.
   * 
   * @param label Message/headline which appears before the SBools.
   * @param toPrint The SBools to print.
   */
  public void openAndPrint(String label, List<Computation<SBool>> toPrint);

  /**
   * When evaluated, opens the given SBools and prints them to the given stream.
   * 
   * @param label Message/headline which appears before the SBools.
   * @param toPrint The SBools to print.
   * @param stream The stream to print to
   */
  public void openAndPrint(String label, List<Computation<SBool>> toPrint, PrintStream stream);

  /**
   * Print the given message to System.out when evaluated.
   * 
   * @param message The message to print.
   */
  public void marker(String message);

  /**
   * Print the given message to the given stream when evaluated.
   * 
   * @param message The message to print.
   * @param stream The stream to print to.
   */
  public void marker(String message, PrintStream stream);
}
