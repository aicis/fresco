package dk.alexandra.fresco.lib.bool;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.binary.BinaryBuilder;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BooleanHelper {

  public static List<Computation<SBool>> known(Boolean[] bools, BinaryBuilder builder) {
    return Arrays.asList(bools).stream().map(builder::known).collect(Collectors.toList());
  }
}
