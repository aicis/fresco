package dk.alexandra.fresco.lib.bool;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.binary.Binary;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BooleanHelper {

  public static List<DRes<SBool>> known(Boolean[] bools, Binary builder) {
    return Arrays.asList(bools).stream().map(builder::known).collect(Collectors.toList());
  }
}
