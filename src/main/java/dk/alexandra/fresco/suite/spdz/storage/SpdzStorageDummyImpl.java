package dk.alexandra.fresco.suite.spdz.storage;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;

public class SpdzStorageDummyImpl implements SpdzStorage{
	
	private List<BigInteger> opened_values;
	private List<SpdzElement> closed_values;
	
	private DataSupplier supplier;
	
	public SpdzStorageDummyImpl(int myId, int numberOfParties) {		
		opened_values = new LinkedList<BigInteger>();
		closed_values = new LinkedList<SpdzElement>();
		
		supplier = new DummyDataSupplierImpl(myId, numberOfParties);
	}
	
	@Override
	public void shutdown() {
		// Does nothing..
	}

	@Override
	public void reset() {
		opened_values.clear();
		closed_values.clear();
	}

	@Override
	public DataSupplier getSupplier() {
		return this.supplier;
	}

	@Override
	public void addOpenedValue(BigInteger val) {
		opened_values.add(val);
	}

	@Override
	public void addClosedValue(SpdzElement elem) {
		closed_values.add(elem);
	}

	@Override
	public List<BigInteger> getOpenedValues() {
		return opened_values;
	}

	@Override
	public List<SpdzElement> getClosedValues() {
		return closed_values;
	}

	@Override
	public BigInteger getSSK() {
		return this.supplier.getSSK();
	}

}
