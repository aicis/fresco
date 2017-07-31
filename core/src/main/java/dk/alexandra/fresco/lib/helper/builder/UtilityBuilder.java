package dk.alexandra.fresco.lib.helper.builder;

import java.io.PrintStream;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.debug.BinaryOpenAndPrint;
import dk.alexandra.fresco.lib.debug.MarkerProtocolImpl;
import dk.alexandra.fresco.lib.debug.ArithmeticOpenAndPrint;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.CopyProtocolImpl;

/**
 * Builder that provides utility functionality primarily for debugging purposes.
 * 
 * @author Kasper Damgaard
 *
 */
public class UtilityBuilder extends AbstractProtocolBuilder {

	private ProtocolFactory factory;

	/**
	 * 
	 * @param factory
	 *            Needs to be either a BasicNumericFactory or a
	 *            BasicLogicFactory
	 */
	public UtilityBuilder(ProtocolFactory factory) {
		this.factory = factory;
	}

	@Override
	public void addProtocolProducer(ProtocolProducer pp) {
		this.append(pp);
	}

	/**
	 * Prints the given string whenever the evaluator gets to this marker.
	 * 
	 * @param message
	 *            The message to print
	 */
	public void createMarker(String message) {
		this.append(new MarkerProtocolImpl(message, null));
	}

	/**
	 * Copies the given value into the returned value
	 * 
	 * @param toCopy
	 *            The value to copy
	 * @return A new object containing the same information as is within toCopy.
	 */
	public SInt copy(SInt toCopy) {
		BasicNumericFactory bnf = (BasicNumericFactory) factory;
		SInt into = bnf.getSInt();
		this.append(new CopyProtocolImpl<SInt>(toCopy, into));
		return into;
	}

	/**
	 * Copies the given value into the returned value
	 * 
	 * @param toCopy
	 *            The value to copy
	 * @return A new object containing the same information as is within toCopy.
	 */
	public SBool copy(SBool toCopy) {
		BasicLogicFactory blf = (BasicLogicFactory) factory;
		SBool into = blf.getSBool();
		this.append(new CopyProtocolImpl<SBool>(toCopy, into));
		return into;
	}

	/**
	 * Creates an open to all protocol and prints the content of the value given
	 * after the given message is printed.
	 * 
	 * @param message
	 *            The message to mark this opening with.
	 * @param value
	 *            The value to open and print.
	 */
	public void openAndPrint(String message, SInt value) {
		BasicNumericFactory bnf = (BasicNumericFactory) factory;
		ArithmeticOpenAndPrint oapp = new ArithmeticOpenAndPrint(message, value, bnf);
		this.append(oapp);
	}

	 /**
   * Creates an open to all protocol and prints the content of the value given
   * after the given message is printed.
   * 
   * @param message
   *            The message to mark this opening with.
   * @param value
   *            The value to open and print.
   */
  public void openAndPrint(String message, SBool value) {
    openAndPrint(message, new SBool[]{value});
  }

  /**
  * Creates an open to all protocol and prints the content of the value given
  * after the given message is printed.
  * 
  * @param message
  *            The message to mark this opening with.
  * @param value
  *            The value to open and print.
  */
 public void openAndPrint(String message, SBool[] value) {
   BasicLogicFactory bnf = (BasicLogicFactory) factory;
   BinaryOpenAndPrint oapp = new BinaryOpenAndPrint(message, value, bnf, null);
   this.append(oapp);
 }

 /**
 * Creates an open to all protocol and prints the content of the value given
 * after the given message is printed.
 * 
 * @param message
 *            The message to mark this opening with.
 * @param value
 *            The value to open and print.
 * @param output           
 *            The PrintStream to receive the print
 */
public void openAndPrint(String message, SBool[] value, PrintStream output) {
  BasicLogicFactory bnf = (BasicLogicFactory) factory;
  BinaryOpenAndPrint oapp = new BinaryOpenAndPrint(message, value, bnf, output);
  this.append(oapp);
}

/**
* Creates an open to all protocol and prints the content of the value given
* after the given message is printed.
* 
* @param message
*            The message to mark this opening with.
* @param value
*            The value to open and print.
* @param output           
*            The PrintStream to receive the print
*/
public void openAndPrint(String message, SBool value, PrintStream output) {
 openAndPrint(message,  new SBool[] {value}, output);
}

	/**
	 * Creates an open to all protocol and prints the contents of the values
	 * given after the given message is printed.
	 * 
	 * @param message
	 *            The message to mark this opening with.
	 * @param value
	 *            The value to open and print.
	 */
	public void openAndPrint(String message, SInt[] value) {
		BasicNumericFactory bnf = (BasicNumericFactory) factory;
		ArithmeticOpenAndPrint oapp = new ArithmeticOpenAndPrint(message, value, bnf);
		this.append(oapp);
	}

	/**
	 * Creates an open to all protocol and prints the contents of the values
	 * given after the given message is printed.
	 * 
	 * @param message
	 *            The message to mark this opening with.
	 * @param value
	 *            The value to open and print.
	 */
	public void openAndPrint(String message, SInt[][] value) {
		BasicNumericFactory bnf = (BasicNumericFactory) factory;
		ArithmeticOpenAndPrint oapp = new ArithmeticOpenAndPrint(message, value, bnf);
		this.append(oapp);
	}
}
