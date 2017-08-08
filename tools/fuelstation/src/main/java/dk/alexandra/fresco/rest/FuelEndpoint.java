package dk.alexandra.fresco.rest;

import dk.alexandra.fresco.services.DataGenerator;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fuel")
public class FuelEndpoint {

  @Autowired
  private DataGenerator generator;

  private int modulusSize;

  @PostConstruct
  public void init() {
    byte[] bytes = generator.getModulus().toByteArray();
    if (bytes[0] == 0) {
      modulusSize = bytes.length - 1;
    } else {
      modulusSize = bytes.length;
    }
  }

  /**
   * REST endpoint for fetching a given amount of triples for the wanted party. Note that this is
   * currently insecure as any party can ask for any other party's preprocessed material.
   * 
   * @param amount The amount of triples to procure
   * @param partyId The ID of the owning party
   * @param thread The thread id - used for parallel runs
   * @param response Autowired by spring- needed for streaming the data
   * @throws InterruptedException If the thread fetching from the blocking queue gets interrupted.
   * @throws IOException If the stream gets cut.
   */
  @RequestMapping(value = "/triples/{amount}/party/{partyId}/thread/{thread}",
      method = RequestMethod.GET)
  private void generateTriples(@PathVariable int amount, @PathVariable int partyId,
      @PathVariable int thread, HttpServletResponse response)
          throws InterruptedException, IOException {
    SpdzTriple[] triples = generator.getTriples(amount, partyId, thread);
    // int elmLength = Util.getModulusSize()*2;
    // int contentLength = elmLength*3*amount;
    // response.setContentLength(contentLength);
    OutputStream os = response.getOutputStream();
    for (SpdzTriple t : triples) {
      os.write(spdzElementToByteArray(t.getA()));
      os.write(spdzElementToByteArray(t.getB()));
      os.write(spdzElementToByteArray(t.getC()));
      os.flush();
    }
  }

  /**
   * Same as {@link #generateTriples(int, int, int, HttpServletResponse)}, but for bits instead.
   */
  @RequestMapping(value = "/bits/{amount}/party/{partyId}/thread/{thread}",
      method = RequestMethod.GET)
  private void generateBits(@PathVariable int amount, @PathVariable int partyId,
      @PathVariable int thread, HttpServletResponse response)
          throws InterruptedException, IOException {
    SpdzElement[] bits = generator.getBits(amount, partyId, thread);
    OutputStream os = response.getOutputStream();
    for (SpdzElement b : bits) {
      os.write(spdzElementToByteArray(b));
      os.flush();
    }
  }

  /**
   * Same as {@link #generateTriples(int, int, int, HttpServletResponse)}, but for exponentiation
   * pipes instead.
   */
  @RequestMapping(value = "/exp/{amount}/party/{partyId}/thread/{thread}",
      method = RequestMethod.GET)
  private void generateExpPipes(@PathVariable int amount, @PathVariable int partyId,
      @PathVariable int thread, HttpServletResponse response)
          throws InterruptedException, IOException {
    SpdzElement[][] pipes = generator.getExpPipes(amount, partyId, thread);
    OutputStream os = response.getOutputStream();
    for (SpdzElement[] p : pipes) {
      for (SpdzElement elm : p) {
        os.write(spdzElementToByteArray(elm));
      }
      os.flush();
    }
  }

  /**
   * Same as {@link #generateTriples(int, int, int, HttpServletResponse)}, but for input masks
   * instead
   */
  @RequestMapping(value = "/inputs/{amount}/party/{partyId}/towards/{towardsId}/thread/{thread}",
      method = RequestMethod.GET)
  private void generateInputMasks(@PathVariable int amount, @PathVariable int partyId,
      @PathVariable int towardsId, @PathVariable int thread, HttpServletResponse response)
          throws InterruptedException, IOException {
    SpdzInputMask[] masks = generator.getInputMasks(amount, partyId, towardsId, thread);
    OutputStream os = response.getOutputStream();
    for (SpdzInputMask m : masks) {
      if (m.getRealValue() == null) {
        os.write(0);
      } else {
        byte[] real = m.getRealValue().toByteArray();
        os.write(real.length);
        os.write(real);
      }
      os.write(spdzElementToByteArray(m.getMask()));
      os.flush();
    }
  }

  /**
   * 
   * @return The global modulus used in the MPC system.
   */
  @RequestMapping(value = "/modulus", method = RequestMethod.GET)
  private BigInteger getModulus() {
    return generator.getModulus();
  }

  /**
   * NB: This is insecure as any party can call this end point and obtain the secret key of any
   * party.
   * 
   * @param partyId The ID of the party whoose secret key to obtain.
   * @return The secret key used for the SPDZ protocol suite.
   */
  @RequestMapping(value = "/alpha/{partyId}", method = RequestMethod.GET)
  private BigInteger getAlpha(@PathVariable int partyId) {
    return generator.getAlpha(partyId);
  }

  /**
   * Calling this method has no effect unless all parties called it. When that happens, the effect
   * is the same as restarting the service. This means that all data in queues are thrown away, and
   * generator threads are restarted.
   * 
   * @param partyId The ID of the party calling this end point.
   * @return True if the request was accepted - this does not necessarily mean that the service is
   *         restarted.
   */
  @RequestMapping(value = "/reset/{partyId}", method = RequestMethod.GET)
  private Boolean reset(@PathVariable int partyId) {
    return generator.reset(partyId);
  }


  private byte[] spdzElementToByteArray(SpdzElement e) {
    byte[] share_invert = new byte[modulusSize];
    byte[] mac_invert = new byte[modulusSize];
    copyAndInvertArray(share_invert, e.getShare().toByteArray());
    copyAndInvertArray(mac_invert, e.getMac().toByteArray());
    byte[] res = new byte[modulusSize * 2];
    System.arraycopy(share_invert, 0, res, 0, modulusSize);
    System.arraycopy(mac_invert, 0, res, modulusSize, modulusSize);
    return res;
  }

  private void copyAndInvertArray(byte[] bytes, byte[] byteArray) {
    for (int inx = 0; inx < byteArray.length; inx++) {
      bytes[bytes.length - byteArray.length + inx] = byteArray[inx];
    }
  }
}
