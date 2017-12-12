package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;

public class TestCote {
  private Cote cote;

  @Before
  public void setup() {
    Random rand = new Random();
    // fake network
    Network network = new Network() {
      @Override
      public void send(int partyId, byte[] data) {
      }

      @Override
      public byte[] receive(int partyId) {
        return null;
      }

      @Override
      public int getNoOfParties() {
        return 0;
      }
    };
    this.cote = new Cote(1, 2, 128, 40, rand, network);
  }

  /**** POSITIVE TESTS. ****/

  /**** NEGATIVE TESTS. ****/
  @Test
  public void testIllegalInit() {
    Random rand = new Random();
    // fake network
    Network network = new Network() {
      @Override
      public void send(int partyId, byte[] data) {}

      @Override
      public byte[] receive(int partyId) {
        return null;
      }

      @Override
      public int getNoOfParties() {
        return 0;
      }
    };
    CoteSender badSender;
    CoteReceiver badReceiver;
    boolean thrown = false;
    try {
      badSender = new CoteSender(1, 2, 128, 80, null, network);
    } catch (IllegalArgumentException e) {
      assertEquals("Illegal constructor parameters", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    try {
      badReceiver = new CoteReceiver(1, 2, 128, 80, rand, null);
    } catch (IllegalArgumentException e) {
      assertEquals("Illegal constructor parameters", e.getMessage());
      thrown = true;
    }
    thrown = false;
    try {
      badSender = new CoteSender(1, 2, 0, 80, rand, network);
    } catch (IllegalArgumentException e) {
      assertEquals("Illegal constructor parameters", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    try {
      badReceiver = new CoteReceiver(1, 2, 128, 0, rand, network);
    } catch (IllegalArgumentException e) {
      assertEquals("Illegal constructor parameters", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    try {
      badSender = new CoteSender(1, 2, 127, 80, rand, network);
    } catch (IllegalArgumentException e) {
      assertEquals("Computational security parameter must be divisible by 8", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    try {
      badSender = new CoteSender(1, 2, 128, 60, rand, network);
    } catch (IllegalArgumentException e) {
      assertEquals("Statistical security parameter must be divisible by 8",
          e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
  }

  @Test
  public void testNotInitialized() {
    boolean thrown = false;
    try {
      cote.getSender().extend(128);
    } catch (IllegalStateException e) {
      assertEquals("Not initialized", e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
    thrown = false;
    try {
      byte[] randomness = new byte[128 / 8];
      cote.getReceiver().extend(new StrictBitVector(randomness, 128));
    } catch (IllegalStateException e) {
      assertEquals("Not initialized", e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }

  @Test
  public void testIllegalExtendSender() {
    boolean thrown = false;
    try {
      cote.getSender().extend(127);
    } catch (IllegalArgumentException e) {
      assertEquals("The amount of OTs must be a positive integer divisize by 8",
          e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
    thrown = false;
    try {
      cote.getSender().extend(-1);
    } catch (IllegalArgumentException e) {
      assertEquals("The amount of OTs must be a positive integer",
          e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
    thrown = false;
    try {
      cote.getSender().extend(0);
    } catch (IllegalArgumentException e) {
      assertEquals("The amount of OTs must be a positive integer",
          e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }

  @Test
  public void testIllegalExtendReceiver() {
    boolean thrown = false;
    try {
      StrictBitVector choices = new StrictBitVector(0);
      cote.getReceiver().extend(choices);
    } catch (IllegalArgumentException e) {
      assertEquals("The amount of OTs must be a positive integer",
          e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }

  @Test
  public void testNoSuchAlgorithm()
      throws NoSuchFieldException, SecurityException, IllegalArgumentException,
      IllegalAccessException, NoSuchMethodException {
    Network network = new Network() {
      @Override
      public void send(int partyId, byte[] data) {
      }

      @Override
      public byte[] receive(int partyId) {
        return null;
      }

      @Override
      public int getNoOfParties() {
        return 0;
      }
    };
    Cote ot = new Cote(1, 2, 128, 40, new Random(42), network);
    Field algorithm = CoteShared.class.getDeclaredField("prgAlgorithm");
    // Remove private
    algorithm.setAccessible(true);
    // Test receiver
    algorithm.set(ot.getReceiver(), "something");
    Method method = ot.getReceiver().getClass().getSuperclass()
        .getDeclaredMethod("makePrg", StrictBitVector.class);
    method.setAccessible(true);
    boolean thrown = false;
    try {
      method.invoke(ot.getReceiver(), new StrictBitVector(8));
    } catch (InvocationTargetException e) {
      assertEquals(
          "Random OT extension failed. No malicious behaviour detected.",
          e.getTargetException().getMessage());
      thrown = true;
    }
    assertTrue(thrown);
  }
}
