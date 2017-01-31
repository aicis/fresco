package dk.alexandra.fresco.suite.spdz;

import java.util.logging.Level;

import org.junit.Test;

import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.suite.spdz.storage.DataRestSupplierImpl;

public class TestDataRestSupplier {

	@Test
	public void test() {
		Reporter.init(Level.INFO);
		DataRestSupplierImpl supplier = new DataRestSupplierImpl(1, "http://localhost:8080/api/fuel/");
		System.out.println(supplier.getModulus());
		System.out.println(supplier.getSSK());
		for(int i = 0; i < 102; i++) {
			//System.out.println(supplier.getNextTriple());
			supplier.getNextTriple();
		}
		
		for(int i = 0; i < 102; i++) {
			//System.out.println(supplier.getNextTriple());
			supplier.getNextExpPipe();
		}
		
		for(int i = 0; i < 102; i++) {
			//System.out.println(supplier.getNextTriple());
			supplier.getNextBit();
		}
		
		for(int i = 0; i < 102; i++) {
			//System.out.println(supplier.getNextTriple());
			supplier.getNextInputMask(1);
		}
		
		for(int i = 0; i < 102; i++) {
			//System.out.println(supplier.getNextTriple());
			supplier.getNextInputMask(2);
		}
	}
}
