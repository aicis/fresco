package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import java.security.MessageDigest;

/**
 * Superclass containing the common variables and methods for the sender and
 * receiver parties of random OT extension.
 */
public interface RotShared {

  int getKbitLength();

  int getLambdaSecurityParam();

  Drbg getRand();

  MessageDigest getDigest();

  int getOtherId();

  Network getNetwork();
}
