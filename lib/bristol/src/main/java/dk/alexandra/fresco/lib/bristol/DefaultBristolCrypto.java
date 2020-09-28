package dk.alexandra.fresco.lib.bristol;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.List;

public class DefaultBristolCrypto implements BristolCrypto {

  private ProtocolBuilderBinary builder;

  public DefaultBristolCrypto(ProtocolBuilderBinary builder) {
    this.builder = builder;
  }

  @Override
  public DRes<List<SBool>> mult32x32(List<DRes<SBool>> in1,
      List<DRes<SBool>> in2) {
    BristolCircuitParser parser =
        BristolCircuitParser.readCircuitDescription("circuits/mult_32x32.txt", in1, in2);
    return builder.seq(parser);
  }

  @Override
  public DRes<List<SBool>> AES(List<DRes<SBool>> plainText,
      List<DRes<SBool>> keyMaterial) {
    BristolCircuitParser parser = BristolCircuitParser
        .readCircuitDescription("circuits/AES-non-expanded.txt", plainText, keyMaterial);
    return builder.seq(parser);
  }

  @Override
  public DRes<List<SBool>> SHA1(List<DRes<SBool>> input) {
    // empty list since the parser will only use input1 as inputs.
    List<DRes<SBool>> in2 = new ArrayList<>();
    BristolCircuitParser parser =
        BristolCircuitParser.readCircuitDescription("circuits/sha-1.txt", input, in2);
    return builder.seq(parser);
  }

  @Override
  public DRes<List<SBool>> DES(List<DRes<SBool>> plainText,
      List<DRes<SBool>> keyMaterial) {
    BristolCircuitParser parser = BristolCircuitParser
        .readCircuitDescription("circuits/DES-non-expanded.txt", plainText, keyMaterial);
    return builder.seq(parser);
  }

  @Override
  public DRes<List<SBool>> SHA256(List<DRes<SBool>> input) {
    // empty list since the parser will only use input1 as inputs.
    List<DRes<SBool>> in2 = new ArrayList<>();
    BristolCircuitParser parser =
        BristolCircuitParser.readCircuitDescription("circuits/sha-256.txt", input, in2);
    return builder.seq(parser);
  }

  @Override
  public DRes<List<SBool>> MD5(List<DRes<SBool>> input) {
    // empty list since the parser will only use input1 as inputs.
    List<DRes<SBool>> in2 = new ArrayList<>();
    BristolCircuitParser parser =
        BristolCircuitParser.readCircuitDescription("circuits/md5.txt", input, in2);
    return builder.seq(parser);
  }

}
