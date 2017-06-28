package dk.alexandra.fresco.demo.mimcaggregation;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.crypto.mimc.MiMCEncryption;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class EncryptAndRevealStep implements Application {

  private SInt[][] inputRows;
  private SInt mimcKey;
  private int toEncryptIndex;
  private Value[][] rowsWithOpenedCiphers;

  public EncryptAndRevealStep(SInt[][] inputRows, int toEncryptIndex) {
    super();
    this.inputRows = inputRows;
    this.toEncryptIndex = toEncryptIndex;
    int numElementsPerRow = inputRows[0].length;
    this.rowsWithOpenedCiphers = new Value[inputRows.length][numElementsPerRow + 1];
  }

  public SInt getMimcKey() {
    return mimcKey;
  }

  @Override
  public ProtocolProducer prepareApplication(BuilderFactory producer) {
    return ProtocolBuilder.createApplicationRoot((BuilderFactoryNumeric) producer, (builder) -> {
      BasicNumericFactory numericFactory = builder.getBasicNumericFactory();
      builder.seq((seq) -> builder.numeric().known(BigInteger.ZERO)
      ).seq((key, seq) -> {
        // Generate random value to use as encryption key
        //TODO It should be possible to get a random element in new API
        return numericFactory.getRandomFieldElement(key);
      }).par((key, par) -> {
        //Store key
        mimcKey = key;
        List<Computation<BigInteger>> ciphers = new ArrayList<>(inputRows.length);
        // Encrypt desired column and open resulting cipher text
        for (final SInt[] row : inputRows) {
          ciphers.add(par.createSequentialSub((seq) -> {
            SInt toEncrypt = row[toEncryptIndex];
            Computation<SInt> cipherText = seq.createSequentialSub(
                new MiMCEncryption(toEncrypt, key)
            );
            return seq.numeric().open(cipherText);
          }));

        }
        return () -> ciphers;
      }).seq((ciphers, seq) -> {
        for (int rowIndex = 0; rowIndex < inputRows.length; rowIndex++) {
          final SInt[] row = inputRows[rowIndex];
          // Since our result array has rows that are one element
          // longer than our input, this is correct
          int cipherIndex = row.length;
          // TODO Fix this demo - what is the point of tihs application?
//          rowsWithOpenedCiphers[rowIndex][cipherIndex] = ciphers.get(rowIndex).out();
          System.arraycopy(row, 0, rowsWithOpenedCiphers[rowIndex], 0, row.length);
        }
        return null;
      });
    }).build();
  }

  public Value[][] getRowsWithOpenedCiphers() {
    return rowsWithOpenedCiphers;
  }

}
