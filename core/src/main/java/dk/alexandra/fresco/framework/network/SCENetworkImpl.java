package dk.alexandra.fresco.framework.network;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SCENetworkImpl implements SCENetwork, SCENetworkSupplier {

  private int noOfParties;

  private Map<Integer, ByteBuffer> input;
  private Map<Integer, ByteArrayOutputStream> output;
  private Set<Integer> expectedInputForNextRound;

  public SCENetworkImpl(int noOfParties) {
    this.noOfParties = noOfParties;
    this.output = new HashMap<>();
    this.expectedInputForNextRound = new HashSet<>();
  }

  //ProtocolNetwork
  @Override
  public ByteBuffer receive(int id) {
    return this.input.get(id);
  }

  @Override
  public List<ByteBuffer> receiveFromAll() {
    List<ByteBuffer> res = new ArrayList<>();
    for (int i = 1; i <= noOfParties; i++) {
      res.add(this.input.get(i));
    }
    return res;
  }

  @Override
  public void send(int id, byte[] data) {
    if (id < 1) {
      throw new IllegalArgumentException("Cannot send to an Id smaller than 1");
    }
    ByteArrayOutputStream buffer =
        this.output.computeIfAbsent(id, k -> new ByteArrayOutputStream());
    buffer.write(data, 0, data.length);
  }

  @Override
  public void sendToAll(byte[] data) {
    for (int i = 1; i <= noOfParties; i++) {
      send(i, data);
    }
  }

  @Override
  public void expectInputFromPlayer(int id) {
    if (id < 1) {
      throw new IllegalArgumentException("Cannot send to an Id smaller than 1");
    }
    this.expectedInputForNextRound.add(id);
  }

  @Override
  public void expectInputFromAll() {
    for (int i = 1; i <= noOfParties; i++) {
      this.expectedInputForNextRound.add(i);
    }
  }

  //ProtocolNetworkSupplier

  @Override
  public void setInput(Map<Integer, ByteBuffer> inputForThisRound) {
    this.input = inputForThisRound;
  }

  @Override
  public Map<Integer, byte[]> getOutputFromThisRound() {
    Map<Integer, byte[]> res = new HashMap<>();
    for (int pid : this.output.keySet()) {
      res.put(pid, this.output.get(pid).toByteArray());
    }
    return res;
  }

  @Override
  public Set<Integer> getExpectedInputForNextRound() {
    return this.expectedInputForNextRound;
  }

  @Override
  public void nextRound() {
    this.output.clear();
    this.expectedInputForNextRound.clear();
  }
}
