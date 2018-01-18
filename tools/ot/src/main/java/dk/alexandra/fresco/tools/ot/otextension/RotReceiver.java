package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.util.List;

/**
 * Protocol class for the party acting as the receiver in an random OT extension
 * following the Bristol 2015 OT extension.
 */
public interface RotReceiver extends RotShared{

  /**
   * Constructs a new batch of random OTs.
   *
   * @param choices
   *          The receivers choices for this extension. This MUST have size
   *          2^x-kbitLength-getLambdaSecurityParam for some x > 3.
   * @return A list of pairs consisting of the bit choices, followed by the
   *         received messages
   */
  List<StrictBitVector> extend(StrictBitVector choices);
}
