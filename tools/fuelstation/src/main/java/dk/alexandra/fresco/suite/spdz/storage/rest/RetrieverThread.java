package dk.alexandra.fresco.suite.spdz.storage.rest;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.FakeTripGen;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.concurrent.Semaphore;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.ByteArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetrieverThread extends Thread {

  private final static Logger logger = LoggerFactory.getLogger(RetrieverThread.class);

  private final int modulusSize;
  private String restEndPoint;
  private final int myId;
  private SpdzRestDataSupplier supplier;
  private final Type type;
  private final int amount;
  private final int towardsId;
  private final int threadId;
  private final Semaphore semaphore;

  private static final int waitTimeInMs = 1000;
  private boolean running = true;
  private final BigInteger modulus;

  RetrieverThread(String restEndPoint, int myId, SpdzRestDataSupplier supplier, Type type,
      int amount, int threadId) {
    this(restEndPoint, myId, supplier, type, amount, threadId, -1);
  }

  RetrieverThread(String restEndPoint, int myId, SpdzRestDataSupplier supplier, Type type,
      int amount, int threadId, int towardsId) {
    super();
    this.modulus = supplier.getModulus();
    byte[] bytes = modulus.toByteArray();
    if (bytes[0] == 0) {
      this.modulusSize = bytes.length - 1;
    } else {
      this.modulusSize = bytes.length;
    }
    this.restEndPoint = restEndPoint;
    this.myId = myId;
    this.supplier = supplier;
    this.type = type;
    this.amount = amount;
    this.threadId = threadId;
    this.towardsId = towardsId;
    this.semaphore = new Semaphore(1);
  }

  void stopThread() {
    running = false;
  }

  private static byte[] readData(InputStream is, int toFillLength) throws IOException {
    ByteArrayBuffer buffer = new ByteArrayBuffer(toFillLength);
    int l = 0;
    int total = 0;
    byte[] tmp = new byte[toFillLength];
    while (true) {
      l = is.read(tmp);
      buffer.append(tmp, 0, l);
      total += l;
      if (total < toFillLength) {
        // We have not read everything, so read some more.
        tmp = new byte[toFillLength - total];
      } else {
        break;
      }
    }
    return buffer.toByteArray();
  }

  @Override
  public void run() {
    CloseableHttpClient httpClient = null;
    while (running) {
      try {
        this.semaphore.acquire();
        httpClient = HttpClients.createDefault();

        HttpGet httpget = null;
        if (towardsId > -1) {
          httpget = new HttpGet(this.restEndPoint + type.getRestName() + "/" + amount + "/party/"
              + this.myId + "/towards/" + towardsId + "/thread/" + threadId);
        } else {
          httpget = new HttpGet(this.restEndPoint + type.getRestName() + "/" + amount + "/party/"
              + this.myId + "/thread/" + threadId);
        }

        logger.debug("Executing request " + httpget.getRequestLine());

        // Create a custom response handler
        ResponseHandler<Void> responseHandler = response -> {
          int status = response.getStatusLine().getStatusCode();
          if (status >= 200 && status < 300) {
            int contentLength = (int) response.getEntity().getContentLength();
            if (contentLength < 0) {
              contentLength = 4096;
            }
            InputStream instream = response.getEntity().getContent();

            // byte[] content = EntityUtils.toByteArray(response.getEntity());
            // ByteArrayInputStream is = new ByteArrayInputStream(content);
            //
            if (!running) {
              // Shut down thread. Resources are dead.
              return null;
            }
            int elmSize = modulusSize * 2;
            byte[] elm;
            try {
              switch (type) {
                case TRIPLE:
                  SpdzTriple t;
                  for (int i = 0; i < amount; i++) {
                    byte[] a = readData(instream, elmSize);
                    byte[] b = readData(instream, elmSize);
                    byte[] c = readData(instream, elmSize);
                    t = new SpdzTriple(new SpdzElement(a, modulus, modulusSize),
                        new SpdzElement(b, modulus, modulusSize),
                        new SpdzElement(c, modulus, modulusSize));
                    supplier.addTriple(t);
                  }
                  break;
                case EXP:
                  for (int i = 0; i < amount; i++) {
                    SpdzElement[] exp = new SpdzElement[FakeTripGen.EXP_PIPE_SIZE];
                    for (int inx = 0; inx < exp.length; inx++) {
                      elm = readData(instream, elmSize);
                      exp[inx] = new SpdzElement(elm, modulus, modulusSize);
                    }
                    supplier.addExp(exp);
                  }
                  break;
                case BIT:
                  for (int i = 0; i < amount; i++) {
                    elm = readData(instream, elmSize);
                    supplier.addBit(new SpdzElement(elm, modulus, modulusSize));
                  }
                  break;
                case INPUT:
                  for (int i = 0; i < amount; i++) {
                    int length = instream.read();
                    if (length == 0) {
                      elm = readData(instream, elmSize);
                      supplier.addInput(
                          new SpdzInputMask(new SpdzElement(elm, modulus, modulusSize)),
                          towardsId);
                    } else {
                      byte[] real = readData(instream, length);
                      elm = readData(instream, elmSize);

                      supplier
                          .addInput(new SpdzInputMask(new SpdzElement(elm, modulus, modulusSize),
                              new BigInteger(real)), towardsId);
                    }
                  }
                  break;
                default:
                  throw new IllegalStateException("Unrecognized Type: " + type);
              }
            } catch (InterruptedException e) {
              running = false;
            }
            // TODO: Consider releasing at the start to start fetching new stuff ASAP.
            semaphore.release();
          } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
          }
          return null;
        };
        httpClient.execute(httpget, responseHandler);
      } catch (InterruptedException e1) {
        running = false;
        throw new MPCException("Retriever got interrupted", e1);
      } catch (ClientProtocolException e) {
        logger.warn("Retriever could not reach client. Exception message: " + e.getMessage()
            + ". Waiting for a " + waitTimeInMs + "ms before trying again.");
        try {
          Thread.sleep(waitTimeInMs);
          semaphore.release();
        } catch (InterruptedException e1) {
          running = false;
          throw new MPCException("Retriever Got interrupted while waiting for client to start.",
              e1);
        }
      } catch (IOException e) {
        running = false;
        throw new MPCException("Retriever ran into an IOException:" + e.getMessage(), e);
      } finally {
        try {
          httpClient.close();
        } catch (IOException e) {
          // silent crashing - nothing to do at this point.
        }
      }
    }
  }
}
