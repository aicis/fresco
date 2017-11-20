package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.FakeTripGen;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TestFakeTripGen {

	@Test
	public void testTripleGen() {
		int amount = 100000;
		int noOfParties = 2;
		BigInteger modulus = new BigInteger("6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
		BigInteger alpha = new BigInteger("5081587041441179438932635098620319894716368628029284292880408086703438041331200877980213770035569812296677935118715454650749402237663859711459266577679205");
		List<SpdzTriple[]> triples = FakeTripGen.generateTriples(amount, noOfParties, modulus, alpha);
		for(SpdzTriple[] t : triples) {
			BigInteger a = t[0].getA().getShare().add(t[1].getA().getShare()).mod(modulus);
			BigInteger b = t[0].getB().getShare().add(t[1].getB().getShare()).mod(modulus);
			BigInteger c = t[0].getC().getShare().add(t[1].getC().getShare()).mod(modulus);
			
			BigInteger shareA = t[0].getA().getMac().add(t[1].getA().getMac()).mod(modulus);
			BigInteger shareB = t[0].getB().getMac().add(t[1].getB().getMac()).mod(modulus);
			BigInteger shareC = t[0].getC().getMac().add(t[1].getC().getMac()).mod(modulus);
			
			Assert.assertEquals(c, a.multiply(b).mod(modulus));
			
			Assert.assertEquals(BigInteger.ZERO, shareA.subtract(a.multiply(alpha).mod(modulus)));
			Assert.assertEquals(BigInteger.ZERO, shareB.subtract(b.multiply(alpha).mod(modulus)));
			Assert.assertEquals(BigInteger.ZERO, shareC.subtract(c.multiply(alpha).mod(modulus)));
		}
	}
	
	@Test
	public void testInputMasks() {
		int amount = 100000;
		int noOfParties = 2;
		BigInteger modulus = new BigInteger("6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
		BigInteger alpha = new BigInteger("5081587041441179438932635098620319894716368628029284292880408086703438041331200877980213770035569812296677935118715454650749402237663859711459266577679205");
		List<List<SpdzInputMask[]>> inps = FakeTripGen
				.generateInputMasks(amount, noOfParties, modulus, alpha);
		for(int towardsPlayer = 1; towardsPlayer < noOfParties+1; towardsPlayer++) {
			List<SpdzInputMask[]> inputMasks = inps.get(towardsPlayer-1);
			for(SpdzInputMask[] masks : inputMasks) {
				Assert.assertNotNull(masks[towardsPlayer-1].getRealValue());
			}
		}
	}
	
	@Test
    public void testInputMasksTowards() {
        int amount = 100000;
        int noOfParties = 2;
        int towardsPlayer = 1;
        BigInteger modulus = new BigInteger("6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
        BigInteger alpha = new BigInteger("5081587041441179438932635098620319894716368628029284292880408086703438041331200877980213770035569812296677935118715454650749402237663859711459266577679205");
        List<SpdzInputMask[]> inputMasks = FakeTripGen
            .generateInputMasks(amount, towardsPlayer, noOfParties, alpha, modulus);        
        for(SpdzInputMask[] masks : inputMasks) {
          Assert.assertNotNull(masks[towardsPlayer-1].getRealValue());
        }
    }
	
	@Test
    public void testExpPipe() {
        int amount = 2;
        int noOfParties = 2;
        BigInteger modulus = new BigInteger("6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
        BigInteger alpha = new BigInteger("5081587041441179438932635098620319894716368628029284292880408086703438041331200877980213770035569812296677935118715454650749402237663859711459266577679205");
        List<SpdzSInt[][]> expPipes = FakeTripGen.generateExpPipes(amount, noOfParties, modulus, alpha);        
        for(SpdzSInt[][] pipe : expPipes) {
          SpdzSInt[] as = pipe[0];
          SpdzSInt[] bs = pipe[1];
          for(int i = 0; i < as.length; i++) {
            BigInteger share = as[i].value.getShare().add(bs[i].value.getShare()).mod(modulus);
            BigInteger mac = as[i].value.getMac().add(bs[i].value.getMac()).mod(modulus);
            Assert.assertEquals(BigInteger.ZERO, mac.subtract(share.multiply(alpha).mod(modulus)));       
          }
        }
    }
	
	@Test
	public void testBitGen() {
	  int amount = 100000;
	  int noOfParties = 2;
	  BigInteger modulus = new BigInteger("6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
	  BigInteger alpha = new BigInteger("5081587041441179438932635098620319894716368628029284292880408086703438041331200877980213770035569812296677935118715454650749402237663859711459266577679205");
	  List<SpdzSInt[]> bits = FakeTripGen.generateBits(amount, noOfParties, modulus, alpha);
	  for(SpdzSInt[] b : bits) {
	    BigInteger val = b[0].value.getShare().add(b[1].value.getShare()).mod(modulus);
	    BigInteger mac = b[0].value.getMac().add(b[1].value.getMac()).mod(modulus); 
	    
	    Assert.assertTrue(val.equals(BigInteger.ZERO) || val.equals(BigInteger.ONE));
	    Assert.assertEquals(BigInteger.ZERO, mac.subtract(val.multiply(alpha).mod(modulus)));	    
	  }
	}
	
	@Test
	public void testElementToBytes() {	  
	  SpdzElement element = new SpdzElement(BigInteger.valueOf(200), BigInteger.ONE, BigInteger.ZERO);
	  FakeTripGen.elementToBytes(element, 1);
	  try {
	    FakeTripGen.elementToBytes(element, 0);
	    Assert.fail("Should have cast an exception");
	  } catch(MPCException e) {
	    
	  }
	  
	  element = new SpdzElement(BigInteger.ONE, BigInteger.valueOf(200), BigInteger.ZERO);
      FakeTripGen.elementToBytes(element, 1);
      try {
        FakeTripGen.elementToBytes(element, 0);
        Assert.fail("Should have cast an exception");
      } catch(MPCException e) {
        
      }
	}
	
	@Test
	public void testBigIntToBytes() {
	  BigInteger b = BigInteger.valueOf(200);
	  int size = 1;
	  FakeTripGen.bigIntToBytes(b, size);
	  try {
        FakeTripGen.bigIntToBytes(b, 0);
        Assert.fail("Should have cast an exception");
	  } catch(MPCException e) {
	    
	  }
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testMainFailNoArgs() throws IOException {
	  FakeTripGen.main(new String[] {""});
	}
	
	@Test(expected=IllegalArgumentException.class)
    public void testMainFailArgs() throws IOException {
      FakeTripGen.main(new String[] {"-y=true"});
    }
	
	@Test()
    public void testMainMissingArgs() throws IOException {
      FakeTripGen.main(new String[] {"-t=10", "-i=10", "-b=100", "-e=1", "-p=2", "-d=."});
      FakeTripGen.main(new String[] {"-m=187263", "-i=10", "-b=100", "-e=1", "-p=2", "-d=."});
      FakeTripGen.main(new String[] {"-m=187263", "-t=10", "-b=100", "-e=1", "-p=2", "-d=."});
      FakeTripGen.main(new String[] {"-m=187263", "-t=10", "-i=10", "-e=1", "-p=2", "-d=."});
      FakeTripGen.main(new String[] {"-m=187263", "-t=10", "-i=10", "-b=100",  "-p=2", "-d=."});
      FakeTripGen.main(new String[] {"-m=187263", "-t=10", "-i=10", "-b=100", "-e=1",  "-d=."});
      FakeTripGen.main(new String[] {"-m=187263", "-t=10", "-i=10", "-b=100", "-e=1", "-p=2"});
	}
	
	@Test()
    public void testMain() throws IOException {
      FakeTripGen.main(new String[] {"-m=187263", "-t=10", "-i=10", "-b=100", "-e=1", "-p=2", "-d=.", "-r=true"});
      FakeTripGen.main(new String[] {"-m=187263", "-t=10", "-i=10", "-b=100", "-e=1", "-p=2", "-d=."});
      FakeTripGen.main(new String[] {"-m=129", "-t=10", "-i=10", "-b=100", "-e=1", "-p=2", "-d=."});
      try {
        FakeTripGen.cleanup();
      } catch (IOException e) {
        Assert.fail(e.getMessage());
      }
    }
}
