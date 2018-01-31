package dk.alexandra.fresco.framework.util;

import java.util.List;

public interface SecretSharer<T> {

  /**
   * Creates secret shares of input field element.
   *
   * @param input field element to secret-share
   * @param numShares number of shares to generate
   * @return secret shares
   */
  List<T> share(T input, int numShares);

  /**
   * Recombines secret shares into input element.
   *
   * @param shares shares to recombine
   * @return recombines shares
   */
  T recombine(List<T> shares);

}
