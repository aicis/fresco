package dk.alexandra.fresco.lib.crypto;

import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.value.SBool;

public class TestBristolCrypto {

  @Test
  public void testGetSHA256CircuitBadParams() {
/*    DummyFactory bnf = new DummyFactory();
    BristolCryptoFactory fact = new BristolCryptoFactory(bnf);
    
    try{
      fact.getSha256Circuit(null, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    
    try{
      SBool[] input = new SBool[199];
      fact.getSha256Circuit(input, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    try{
      SBool[] input = new SBool[512];
      fact.getSha256Circuit(input, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    try{
      SBool[] input = new SBool[512];
      SBool[] output = new SBool[100];
      fact.getSha256Circuit(input, output);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }*/
  }
  
  @Test
  public void testGetSHA1CircuitBadParams() {
  /*  DummyFactory bnf = new DummyFactory();
    BristolCryptoFactory fact = new BristolCryptoFactory(bnf);
    
    try{
      fact.getSha1Circuit(null, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    
    try{
      SBool[] input = new SBool[199];
      fact.getSha1Circuit(input, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    try{
      SBool[] input = new SBool[512];
      fact.getSha1Circuit(input, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    try{
      SBool[] input = new SBool[512];
      SBool[] output = new SBool[100];
      fact.getSha1Circuit(input, output);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }*/
  }

  @Test
  public void testGetMD5CircuitBadParams() {
 /*   DummyFactory bnf = new DummyFactory();
    BristolCryptoFactory fact = new BristolCryptoFactory(bnf);
    
    try{
      fact.getMD5Circuit(null, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    
    try{
      SBool[] input = new SBool[199];
      fact.getMD5Circuit(input, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    try{
      SBool[] input = new SBool[512];
      fact.getMD5Circuit(input, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    try{
      SBool[] input = new SBool[512];
      SBool[] output = new SBool[100];
      fact.getMD5Circuit(input, output);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }*/
  }
 
  @Test
  public void testGetAESCircuitBadParams() {
 /*   DummyFactory bnf = new DummyFactory();
    BristolCryptoFactory fact = new BristolCryptoFactory(bnf);
    
    try{
      fact.getAesProtocol(null, null, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    
    try{
      SBool[] key = new SBool[199];
      fact.getAesProtocol(null, key, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    try{
      SBool[] key = new SBool[128];
      fact.getAesProtocol(null, key, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    try{
      SBool[] key = new SBool[128];
      SBool[] plain = new SBool[100];
      fact.getAesProtocol(plain, key, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }  

    try{
      SBool[] key = new SBool[128];
      SBool[] plain = new SBool[128];
      fact.getAesProtocol(plain, key, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }  

    try{
      SBool[] key = new SBool[128];
      SBool[] plain = new SBool[128];
      SBool[] cipher = new SBool[127];
      fact.getAesProtocol(plain, key, cipher);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }    */
  }

  @Test
  public void testGetDESCircuitBadParams() {
 /*   DummyFactory bnf = new DummyFactory();
    BristolCryptoFactory fact = new BristolCryptoFactory(bnf);
    
    try{
      fact.getDesCircuit(null, null, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    
    try{
      SBool[] key = new SBool[199];
      fact.getDesCircuit(null, key, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    try{
      SBool[] key = new SBool[64];
      fact.getDesCircuit(null, key, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    try{
      SBool[] key = null;
      SBool[] plain = new SBool[64];
      fact.getDesCircuit(plain, key, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }  

    try{
      SBool[] key = new SBool[100];
      SBool[] plain = new SBool[64];
      fact.getDesCircuit(plain, key, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }  
    
    try{
      SBool[] key = new SBool[64];
      SBool[] plain = new SBool[64];
      fact.getDesCircuit(plain, key, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }  

    try{
      SBool[] key = new SBool[64];
      SBool[] plain = new SBool[64];
      SBool[] cipher = new SBool[128];
      fact.getDesCircuit(plain, key, cipher);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }*/    
  }

  @Test
  public void testGetMultX32CircuitBadParams() {
  /*  DummyFactory bnf = new DummyFactory();
    BristolCryptoFactory fact = new BristolCryptoFactory(bnf);
    
    try{
      fact.getMult32x32Circuit(null, null, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    
    try{
      SBool[] a = new SBool[199];
      fact.getMult32x32Circuit(a, null, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    try{
      SBool[] a = new SBool[32];
      fact.getMult32x32Circuit(a, null, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    try{
      SBool[] a = new SBool[32];
      SBool[] b = new SBool[100];
      fact.getMult32x32Circuit(a, b, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }  

    try{
      SBool[] a = new SBool[32];
      SBool[] b = new SBool[32];
      fact.getMult32x32Circuit(a, b, null);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }  

    try{
      SBool[] a = new SBool[32];
      SBool[] b = new SBool[32];
      SBool[] c = new SBool[128];
      fact.getMult32x32Circuit(a, b, c);
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }*/    
  }
  
}
