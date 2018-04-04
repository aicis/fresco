package dk.alexandra.fresco.suite.spdz2k.datatypes;

/**
 * Allows for conversion between {@link UInt} instances and {@link CompUInt} that are composed of
 * them.
 */
public interface CompUIntConverter<
    HighT extends UInt<HighT>,
    LowT extends UInt<LowT>,
    CompT extends CompUInt<HighT, LowT, CompT>> {

  /**
   * Creates new {@link CompT} from an instance of {@link HighT}.
   */
  CompT createFromHigh(HighT value);

  /**
   * Creates new {@link CompT} from an instance of {@link LowT}.
   */
  CompT createFromLow(LowT value);

}
