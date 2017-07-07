package dk.alexandra.fresco.demo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dk.alexandra.fresco.demo.EncryptAndRevealStep.RowWithCipher;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.value.SInt;

public class AggregateStep implements Application<List<List<SInt>>, SequentialNumericBuilder> {


  private List<Triple<SInt, SInt, BigInteger>> triples;
  private int keyColumn;
  private int aggColumn;

  public AggregateStep(List<RowWithCipher> inputRows, int keyColumn, int aggColumn) {
    super();
    this.keyColumn = keyColumn;
    this.aggColumn = aggColumn;
    this.triples = new ArrayList<>(inputRows.size());
    convertToTriples(inputRows);
  }

  private void convertToTriples(List<RowWithCipher> inputRows) {
    for (RowWithCipher row : inputRows) {
      Triple<SInt, SInt, BigInteger> triple = new Triple<SInt, SInt, BigInteger>(
          row.row.get(this.keyColumn),
          row.row.get(this.aggColumn),
          row.cipher
      );
      this.triples.add(triple);
    }
  }

  @Override
  public Computation<List<List<SInt>>> prepareApplication(SequentialNumericBuilder builder) {
    Map<BigInteger, Computation<SInt>> groupedByCipher = new HashMap<>();
    Map<BigInteger, SInt> cipherToShare = new HashMap<>();

    Computation<SInt> zero = builder.numeric().known(BigInteger.ZERO);

    for (Triple<SInt, SInt, BigInteger> triple : this.triples) {
      BigInteger cipher = triple.cipher;
      SInt key = triple.key;
      SInt value = triple.value;

      if (!groupedByCipher.containsKey(cipher)) {
        groupedByCipher.put(cipher, zero);
        cipherToShare.put(cipher, key);
      }

      Computation<SInt> subTotal = builder.numeric().add(groupedByCipher.get(cipher), value);
      groupedByCipher.put(cipher, subTotal);
    }

    return builder.createSequentialSub(seq -> {
      List<List<SInt>> result = new ArrayList<>(groupedByCipher.size());
      for (Entry<BigInteger, Computation<SInt>> keyAndValues : groupedByCipher.entrySet()) {
        ArrayList<SInt> row = new ArrayList<>(2);
        row.add(cipherToShare.get(keyAndValues.getKey()));
        row.add(keyAndValues.getValue().out());
        result.add(row);
      }
      return () -> result;
    });
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
  }
}
