package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.helper.bristol.BristolCircuitParser;
import java.util.ArrayList;
import java.util.List;

public class DefaultBristolCryptoBuilder implements BristolCryptoBuilder {

  private ProtocolBuilderBinary builder;

  public DefaultBristolCryptoBuilder(ProtocolBuilderBinary builder) {
    this.builder = builder;
  }

  @Override
  public Computation<List<SBool>> mult32x32(List<Computation<SBool>> in1,
      List<Computation<SBool>> in2) {
    BristolCircuitParser parser =
        BristolCircuitParser.readCircuitDescription("circuits/mult_32x32.txt", in1, in2);
    return builder.seq(parser);
  }

  @Override
  public Computation<List<SBool>> AES(List<Computation<SBool>> keyMaterial,
      List<Computation<SBool>> plainText) {
    BristolCircuitParser parser = BristolCircuitParser
        .readCircuitDescription("circuits/AES-non-expanded.txt", keyMaterial, plainText);
    return builder.seq(parser);
  }

  @Override
  public Computation<List<SBool>> SHA1(List<Computation<SBool>> input) {
    // empty list since the parser will only use input1 as inputs.
    List<Computation<SBool>> in2 = new ArrayList<>();
    BristolCircuitParser parser =
        BristolCircuitParser.readCircuitDescription("circuits/sha-1.txt", input, in2);
    return builder.seq(parser);
  }

  @Override
  public Computation<List<SBool>> DES(List<Computation<SBool>> plainText,
      List<Computation<SBool>> keyMaterial) {
    BristolCircuitParser parser = BristolCircuitParser
        .readCircuitDescription("circuits/DES-non-expanded.txt", plainText, keyMaterial);
    return builder.seq(parser);
  }

  @Override
  public Computation<List<SBool>> SHA256(List<Computation<SBool>> input) {
    // empty list since the parser will only use input1 as inputs.
    List<Computation<SBool>> in2 = new ArrayList<>();
    BristolCircuitParser parser =
        BristolCircuitParser.readCircuitDescription("circuits/sha-256.txt", input, in2);
    return builder.seq(parser);
  }

  @Override
  public Computation<List<SBool>> MD5(List<Computation<SBool>> input) {
    // empty list since the parser will only use input1 as inputs.
    List<Computation<SBool>> in2 = new ArrayList<>();
    BristolCircuitParser parser =
        BristolCircuitParser.readCircuitDescription("circuits/md5.txt", input, in2);
    return builder.seq(parser);
  }

}
