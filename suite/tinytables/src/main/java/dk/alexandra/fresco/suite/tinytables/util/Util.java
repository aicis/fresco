package dk.alexandra.fresco.suite.tinytables.util;

public class Util {

  private Util() {
    // Do not instantiate
  }

  public static int otherPlayerId(int myId) {
    return myId == 1 ? 2 : 1;
  }
}
