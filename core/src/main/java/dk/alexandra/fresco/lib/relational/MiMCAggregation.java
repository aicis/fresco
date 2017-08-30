package dk.alexandra.fresco.lib.relational;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.shuffle.ShuffleRows;
import dk.alexandra.fresco.lib.crypto.mimc.MiMCEncryption;

public class MiMCAggregation implements ComputationBuilder<Matrix<Computation<SInt>>> {

  final private Matrix<Computation<SInt>> values;
  final private Random rand;
  final private int groupColIdx;
  final private int aggColIdx;
  final private int pid;
  final private int[] pids;

  public MiMCAggregation(Matrix<Computation<SInt>> values, int groupColIdx, int aggColIdx, int pid,
      int[] pids) {
    this(values, new SecureRandom(), groupColIdx, aggColIdx, pid, pids);
  }

  MiMCAggregation(Matrix<Computation<SInt>> values, Random rnd, int groupColIdx, int aggColIdx,
      int pid, int[] pids) {
    super();
    this.values = values;
    this.rand = rnd;
    this.groupColIdx = groupColIdx;
    this.aggColIdx = aggColIdx;
    this.pid = pid;
    this.pids = pids;
  }

  private Matrix<Computation<SInt>> toMatrix(Map<BigInteger, Computation<SInt>> groupedByCipher,
      Map<BigInteger, Computation<SInt>> cipherToShare) {
    ArrayList<ArrayList<Computation<SInt>>> result = new ArrayList<>(groupedByCipher.size());
    for (Entry<BigInteger, Computation<SInt>> keyAndValues : groupedByCipher.entrySet()) {
      ArrayList<Computation<SInt>> row = new ArrayList<>(2);
      row.add(cipherToShare.get(keyAndValues.getKey()));
      row.add(keyAndValues.getValue());
      result.add(row);
    }
    return new Matrix<>(result.size(), 2, result);
  }

  @Override
  public Computation<Matrix<Computation<SInt>>> build(SequentialNumericBuilder builder) {
    // shuffle input
    Computation<Matrix<Computation<SInt>>> shuffled =
        builder.createSequentialSub(new ShuffleRows(values, rand, pid, pids));
    // generate encryption key
    Computation<SInt> mimcKey = builder.numeric().randomElement();
    return builder.par(par -> {
      Matrix<Computation<SInt>> inputRows = shuffled.out();
      // encrypt values in group by column and reveal cipher text
      List<TripleWithCipher> ciphers = new ArrayList<>(inputRows.getHeight());
      for (final ArrayList<Computation<SInt>> row : inputRows.getRows()) {
        Computation<SInt> groupBy = row.get(groupColIdx);
        Computation<SInt> aggOn = row.get(aggColIdx);
        // encrypt and open groupBy
        Computation<BigInteger> openedCipher = par.createSequentialSub((seq -> {
          Computation<SInt> cipherText =
              seq.createSequentialSub(new MiMCEncryption(groupBy, mimcKey));
          return seq.numeric().open(cipherText);
        }));
        ciphers.add(new TripleWithCipher(groupBy, aggOn, openedCipher));
      }
      return () -> ciphers;
    }).seq((triples, seq) -> {
      // use cipher texts to perform aggregation "in-the-clear"
      Map<BigInteger, Computation<SInt>> groupedByCipher = new HashMap<>();
      Map<BigInteger, Computation<SInt>> cipherToShare = new HashMap<>();

      for (TripleWithCipher triple : triples) {
        BigInteger cipher = triple.getCipher().out();
        Computation<SInt> key = triple.getKey();
        Computation<SInt> value = triple.getValue();

        if (!groupedByCipher.containsKey(cipher)) {
          groupedByCipher.put(cipher, value);
          cipherToShare.put(cipher, key);
        } else {
          Computation<SInt> subTotal = seq.numeric().add(groupedByCipher.get(cipher), value);
          groupedByCipher.put(cipher, subTotal);
        }
      }
      return () -> toMatrix(groupedByCipher, cipherToShare);
    });
  }

  private class TripleWithCipher
      extends Triple<Computation<SInt>, Computation<SInt>, Computation<BigInteger>> {

    public TripleWithCipher(Computation<SInt> key, Computation<SInt> value,
        Computation<BigInteger> cipher) {
      super(key, value, cipher);
    }

  }

  private class Triple<K, V, C> {

    private K key;
    private V value;
    private C cipher;

    public Triple(K key, V value, C cipher) {
      super();
      this.key = key;
      this.value = value;
      this.cipher = cipher;
    }

    public K getKey() {
      return key;
    }

    public V getValue() {
      return value;
    }

    public C getCipher() {
      return cipher;
    }

  }

}
