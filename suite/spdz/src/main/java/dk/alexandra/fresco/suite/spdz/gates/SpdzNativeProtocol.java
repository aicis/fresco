package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class SpdzNativeProtocol<OutputT> implements
    NativeProtocol<OutputT, SpdzResourcePool> {

  byte[] sendBroadcastValidation(MessageDigest dig, Network network, BigInteger b) {
    dig.update(b.toByteArray());
    return sendAndReset(dig, network);
  }

  byte[] sendBroadcastValidation(MessageDigest dig, Network network,
      Collection<BigInteger> bs) {
    for (BigInteger b : bs) {
      dig.update(b.toByteArray());
    }
    return sendAndReset(dig, network);
  }

  private byte[] sendAndReset(MessageDigest dig, Network network) {
    byte[] digest = dig.digest();
    dig.reset();
    network.sendToAll(digest);
    return digest;
  }

  boolean receiveBroadcastValidation(Network network, byte[] digest) {
    //TODO: should we check that we get messages from all players?
    boolean validated = true;
    List<byte[]> digests = network.receiveFromAll();
    for (byte[] d : digests) {
      boolean equals = Arrays.equals(d, digest);
      validated = validated && equals;
    }
    return validated;
  }
}
