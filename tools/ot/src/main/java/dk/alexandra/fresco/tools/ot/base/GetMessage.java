package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.otextension.PseudoOtp;

public abstract class GetMessage {
     StrictBitVector getUnpaddedMessage(boolean choiceBit, byte[] seed, byte[] encryptedZeroMessage, byte[] encryptedOneMessage) {
        if (encryptedZeroMessage.length != encryptedOneMessage.length) {
            throw new MaliciousException("The length of the two choice messages is not equal");
        }
        byte[] unpaddedMessage;
        if (!choiceBit) {
            unpaddedMessage = PseudoOtp.decrypt(encryptedZeroMessage, seed);
        } else {
            unpaddedMessage = PseudoOtp.decrypt(encryptedOneMessage, seed);
        }
        return new StrictBitVector(unpaddedMessage);
    }
}
