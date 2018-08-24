package dk.alexandra.fresco.suite.spdz2k.resource.storage;

import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.OpenedValueStoreImpl;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;

/**
 * Spdz2k-specific instantiation of {@link OpenedValueStore}.
 */
public class Spdz2kOpenedValueStoreImpl<PlainT extends CompUInt<?, ?, PlainT>>
    extends OpenedValueStoreImpl<Spdz2kSInt<PlainT>, PlainT> {

}
