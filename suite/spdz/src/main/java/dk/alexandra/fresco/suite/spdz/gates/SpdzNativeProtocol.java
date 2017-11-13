package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.network.serializers.ByteArrayHelper;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class SpdzNativeProtocol<OutputT> implements
    NativeProtocol<OutputT, SpdzResourcePool> {

  byte[] sendBroadcastValidation(MessageDigest dig, SCENetwork network, BigInteger b) {
    dig.update(b.toByteArray());
    byte[] digest = dig.digest();
    dig.reset();
    network.sendToAll(ByteArrayHelper.addSize(digest));
    return digest;
  }

  byte[] sendBroadcastValidation(MessageDigest dig, SCENetwork network,
      Collection<BigInteger> bs) {
    for (BigInteger b : bs) {
      dig.update(b.toByteArray());
    }
    byte[] digest = dig.digest();
    dig.reset();
    network.sendToAll(ByteArrayHelper.addSize(digest));
    return digest;
  }

  boolean receiveBroadcastValidation(SCENetwork network, byte[] digest) {
    //TODO: should we check that we get messages from all players?
    boolean validated = true;
    List<ByteBuffer> digests = network.receiveFromAll();
    for (ByteBuffer buffer : digests) {
      byte[] d = ByteArrayHelper.getByteObject(buffer);
      validated = validated && Arrays.equals(d, digest);
    }
    return validated;
  }
}
