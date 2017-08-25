package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.EncryptAndRevealStep.RowWithCipher;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.crypto.mimc.MiMCEncryption;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EncryptAndRevealStep implements
    Application<List<RowWithCipher>, ProtocolBuilderNumeric> {

  private final List<List<SInt>> inputRows;
  private final int toEncryptIndex;

  public EncryptAndRevealStep(List<List<SInt>> inputRows, int toEncryptIndex) {
    super();
    this.inputRows = inputRows;
    this.toEncryptIndex = toEncryptIndex;
  }

  @Override
  public Computation<List<RowWithCipher>> prepareApplication(ProtocolBuilderNumeric builder) {
    return builder.par(par ->
        builder.numeric().randomElement()
    ).par((mimcKey, par) -> {
      List<Pair<List<SInt>, Computation<BigInteger>>> ciphers = new ArrayList<>(inputRows.size());
      // Encrypt desired column and open resulting cipher text
      for (final List<SInt> row : inputRows) {
        ciphers.add(
            new Pair<>(row,
                par.createSequentialSub((seq) -> {
                  SInt toEncrypt = row.get(toEncryptIndex);
                  Computation<SInt> cipherText = seq.seq(
                      new MiMCEncryption(toEncrypt, mimcKey)
                  );
                  return seq.numeric().open(cipherText);
                })));
      }
      return () -> ciphers;
    }).seq((ciphers, seq) -> {
      List<RowWithCipher> resultList = ciphers.stream()
          .map(cipherPair -> new RowWithCipher(cipherPair)).collect(Collectors.toList());
      return () -> resultList;
    });
  }

  public static class RowWithCipher {

    public final List<SInt> row;
    public final BigInteger cipher;


    private RowWithCipher(Pair<List<SInt>, Computation<BigInteger>> cipherPair) {
      this.row = cipherPair.getFirst();
      this.cipher = cipherPair.getSecond().out();
    }
  }
}
