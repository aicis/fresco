package dk.alexandra.fresco.lib.collections.relational;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.crypto.mimc.MiMCEncryption;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MiMCAggregation implements Computation<Matrix<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final DRes<Matrix<DRes<SInt>>> values;
  private final int groupColIdx;
  private final int aggColIdx;

  public MiMCAggregation(DRes<Matrix<DRes<SInt>>> values, int groupColIdx, int aggColIdx) {
    super();
    this.values = values;
    this.groupColIdx = groupColIdx;
    this.aggColIdx = aggColIdx;
  }

  private Matrix<DRes<SInt>> toMatrix(Map<BigInteger, DRes<SInt>> groupedByCipher,
      Map<BigInteger, DRes<SInt>> cipherToShare) {
    ArrayList<ArrayList<DRes<SInt>>> result = new ArrayList<>(groupedByCipher.size());
    for (Entry<BigInteger, DRes<SInt>> keyAndValues : groupedByCipher.entrySet()) {
      ArrayList<DRes<SInt>> row = new ArrayList<>(2);
      row.add(cipherToShare.get(keyAndValues.getKey()));
      row.add(keyAndValues.getValue());
      result.add(row);
    }
    return new Matrix<>(result.size(), 2, result);
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    // shuffle input
    DRes<Matrix<DRes<SInt>>> shuffled = builder.collections().shuffle(values);
    // generate encryption key
    DRes<SInt> mimcKey = builder.numeric().randomElement();
    return builder.par(par -> {
      Matrix<DRes<SInt>> inputRows = shuffled.out();
      // encrypt values in group by column and reveal cipher text
      List<TripleWithCipher> ciphers = new ArrayList<>(inputRows.getHeight());
      for (final ArrayList<DRes<SInt>> row : inputRows.getRows()) {
        DRes<SInt> groupBy = row.get(groupColIdx);
        DRes<SInt> aggOn = row.get(aggColIdx);
        // encrypt and open groupBy
        DRes<BigInteger> openedCipher = par.seq((seq -> {
          // TODO: encryption should be on a directory
          DRes<SInt> cipherText = seq.seq(new MiMCEncryption(groupBy, mimcKey));
          return seq.numeric().open(cipherText);
        }));
        ciphers.add(new TripleWithCipher(groupBy, aggOn, openedCipher));
      }
      return () -> ciphers;
    }).seq((seq, triples) -> {
      // use cipher texts to perform aggregation "in-the-clear"
      Map<BigInteger, DRes<SInt>> groupedByCipher = new HashMap<>();
      Map<BigInteger, DRes<SInt>> cipherToShare = new HashMap<>();

      for (TripleWithCipher triple : triples) {
        BigInteger cipher = triple.getCipher().out();
        DRes<SInt> key = triple.getKey();
        DRes<SInt> value = triple.getValue();

        if (!groupedByCipher.containsKey(cipher)) {
          groupedByCipher.put(cipher, value);
          cipherToShare.put(cipher, key);
        } else {
          DRes<SInt> subTotal = seq.numeric().add(groupedByCipher.get(cipher), value);
          groupedByCipher.put(cipher, subTotal);
        }
      }
      return () -> toMatrix(groupedByCipher, cipherToShare);
    });
  }

  private class TripleWithCipher extends Triple<DRes<SInt>, DRes<SInt>, DRes<BigInteger>> {

    public TripleWithCipher(DRes<SInt> key, DRes<SInt> value, DRes<BigInteger> cipher) {
      super(key, value, cipher);
    }

  }

  private class Triple<K, V, C> {

    private final K key;
    private final V value;
    private final C cipher;

    public Triple(K key, V value, C cipher) {
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
