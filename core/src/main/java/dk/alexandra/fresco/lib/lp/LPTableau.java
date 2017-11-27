package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import java.io.PrintStream;
import java.util.ArrayList;

public class LPTableau {

  // The constraint matrix
  private final Matrix<DRes<SInt>> C;
  // The rightmost column and bottom row of the tableau, except for the last entry of both
  private final ArrayList<DRes<SInt>> B;
  private final ArrayList<DRes<SInt>> F;
  // The the bottom right hand corner entry of the tableau
  private final DRes<SInt> z;

  public LPTableau(Matrix<DRes<SInt>> C, ArrayList<DRes<SInt>> B,
      ArrayList<DRes<SInt>> F, DRes<SInt> z) {
    if (C.getWidth() == F.size() && C.getHeight() == B.size()) {
      this.C = C;
      this.B = B;
      this.F = F;
      this.z = z;
    } else {
      throw new IllegalArgumentException("Dimensions of tableau does not match");
    }
  }

  public Matrix<DRes<SInt>> getC() {
    return C;
  }

  public ArrayList<DRes<SInt>> getB() {
    return B;
  }

  public ArrayList<DRes<SInt>> getF() {
    return F;
  }

  public DRes<SInt> getZ() {
    return z;
  }

  /**
   * Opens and outputs the plaintext values of this tableau for debugging.
   * 
   * @param builder a builder to provide the open and print functionality
   * @param ps a PrintStream on which to print the debugging information
   */
  public void debugInfo(ProtocolBuilderNumeric builder, PrintStream ps) {
    builder.debug().openAndPrint("C: ", C, ps);
    builder.debug().openAndPrint("B: ", B, ps);
    builder.debug().openAndPrint("F: ", F, ps);
    builder.debug().openAndPrint("z: ", z, ps);
  }
}
