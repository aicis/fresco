package dk.alexandra.fresco.services;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.DummyDataSupplierImpl;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Primary
@Component
@PropertySource("classpath:prepro.properties")
public class DummyDataGenerator implements DataGenerator {

  private Map<Integer, DummyDataSupplierImpl> suppliers;

  @Value("${noOfPlayers}")
  private int noOfPlayers;

  @PostConstruct
  public void init() {
    suppliers = new HashMap<Integer, DummyDataSupplierImpl>();
    for (int i = 1; i <= noOfPlayers; i++) {
      DummyDataSupplierImpl supplier = new DummyDataSupplierImpl(i, noOfPlayers);
      suppliers.put(i, supplier);
    }
  }

  @Override
  public BigInteger getModulus() {
    return suppliers.get(1).getModulus();
  }

  @Override
  public BigInteger getAlpha(int partyId) {
    return suppliers.get(partyId).getSecretSharedKey();
  }

  @Override
  public void addTriples(List<SpdzTriple[]> triples, int thread) throws InterruptedException {
    // TODO Auto-generated method stub

  }

  @Override
  public SpdzTriple[] getTriples(int amount, int partyId, int thread) throws InterruptedException {
    SpdzTriple[] res = new SpdzTriple[amount];
    for (int i = 0; i < amount; i++) {
      res[i] = this.suppliers.get(partyId).getNextTriple();
    }
    return res;
  }

  @Override
  public void addBits(List<SpdzSInt[]> bits, int thread) throws InterruptedException {
    // TODO Auto-generated method stub

  }

  @Override
  public SpdzElement[] getBits(int amount, int partyId, int thread) throws InterruptedException {
    SpdzElement[] res = new SpdzElement[amount];
    for (int i = 0; i < amount; i++) {
      res[i] = this.suppliers.get(partyId).getNextBit().value;
    }
    return res;
  }

  @Override
  public void addExpPipes(List<SpdzSInt[][]> expPipes, int thread) throws InterruptedException {
    // TODO Auto-generated method stub

  }

  @Override
  public SpdzElement[][] getExpPipes(int amount, int partyId, int thread)
      throws InterruptedException {
    SpdzElement[][] res = new SpdzElement[amount][];
    for (int i = 0; i < amount; i++) {
      SpdzSInt[] exp = this.suppliers.get(partyId).getNextExpPipe();
      res[i] = new SpdzElement[exp.length];
      int n = 0;
      for (SpdzSInt sInt : exp) {
        res[i][n++] = sInt.value;
      }
    }
    return res;
  }

  @Override
  public void addInputMasks(int i, List<SpdzInputMask[]> inpMasks, int thread)
      throws InterruptedException {
    // TODO Auto-generated method stub

  }

  @Override
  public SpdzInputMask[] getInputMasks(int amount, int partyId, int towardsPartyId, int thread)
      throws InterruptedException {
    SpdzInputMask[] res = new SpdzInputMask[amount];
    for (int i = 0; i < amount; i++) {
      res[i] = this.suppliers.get(partyId).getNextInputMask(towardsPartyId);
    }
    return res;
  }

  @Override
  public Boolean reset(int partyId) {
    return true;
  }

}
