package dk.alexandra.fresco.framework.network;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Test;

public class TestKryoNetNetwork {

  private List<Integer> getFreePorts(int no) {
    try {
      List<Integer> ports = new ArrayList<>();
      List<ServerSocket> socks = new ArrayList<>();
      for (int i = 0; i < no; i++) {
        ServerSocket sock = new ServerSocket(0);
        int port = sock.getLocalPort();  
        ports.add(port);
        socks.add(sock);
      }
      for (ServerSocket s : socks) {
        s.close();
      }
      
      return ports;
    } catch (IOException e) {
      Assert.fail("Could not locate a free port");
      return null;
    }
  }
  
  @Test
  public void testKryoNetSendBytes() {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0), "78dbinb27xi1i"));
    parties.put(2, new Party(2, "localhost", ports.get(1), "h287hs287g22n"));    
    Thread t1 = new Thread(new Runnable() {
      
      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
        KryoNetNetwork network = new KryoNetNetwork(conf);
        network.send(2, new byte[]{0x04});
        int noOfParties = network.getNoOfParties();
        Assert.assertEquals(2, noOfParties);
        try {
          network.close();
        } catch (IOException e) {
          Assert.fail("Should be able to close network");
        }
      }
    });
    
    Thread t2 = new Thread(new Runnable() {
      
      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(2, parties);
        KryoNetNetwork network = new KryoNetNetwork(conf);
        byte[] arr = network.receive(1);
        Assert.assertArrayEquals(new byte[]{0x04}, arr);
        try {
          network.close();
        } catch (IOException e) {
          Assert.fail("Should be able to close network");
        }
      }
    });
    
    t1.start();
    t2.start();
    try {
      t1.join();
      t2.join();
    } catch (InterruptedException e) {
      Assert.fail("Threads should finish without main getting interrupted");
    }    
  }
  
  @Test
  public void testKryoNetSendInterrupt() {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0)));
    parties.put(2, new Party(2, "localhost", ports.get(1)));    
    Thread t1 = new Thread(new Runnable() {
      
      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
        KryoNetNetwork network = new KryoNetNetwork(conf);
        for (int i = 0; i < 1000; i++) {
          network.send(1, new byte[]{0x04});
        }
        try {
          network.send(1, new byte[]{0x04});
          Assert.fail("After 1001 messages the queue should be filled up.");
        } catch (RuntimeException e) {
          //ignore
        }
        try {
          network.close();
        } catch (IOException e) {
          Assert.fail("Should be able to close network");
        }
      }
    });
    
    Thread t2 = new Thread(new Runnable() {
      
      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(2, parties);
        KryoNetNetwork network = new KryoNetNetwork(conf);
        try {
          network.receive(2);
          Assert.fail("Should not be able to receive anything");
        } catch (RuntimeException e) {
          //ignore
        }
        try {
          network.close();
        } catch (IOException e) {
          Assert.fail("Should be able to close network");
        }
      }
    });
    
    t1.start();
    t2.start();
    try {
      t2.join(400);
      t1.join(400);
      t1.interrupt();
      t2.interrupt();
      
      t1.join();
      t2.join();
    } catch (InterruptedException e) {
      Assert.fail("Threads should finish without main getting interrupted");
    }    
  }
  
  @Test
  public void testKryoNetConnectTimeout() {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0)));
    parties.put(2, new Party(2, "localhost", ports.get(1)));    
    Thread t1 = new Thread(new Runnable() {
      
      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
        KryoNetNetwork network = null;
        try {
          network = new KryoNetNetwork(conf);
          Assert.fail("Should not be able to connect");
        } catch (RuntimeException e) {
          //ignore
        } finally {
          try {
            if (network != null) {
              network.close();
            }
          } catch (IOException e) {
            Assert.fail("Should be able to close the network");
          }
        }
      }
    });
    
    t1.start();
    try {      
      t1.join();
      t1.interrupt();
    } catch (InterruptedException e) {
      Assert.fail("Threads should finish without main getting interrupted");
    }    
  }
  
  @Test
  public void testKryoNetConnectInterrupt() {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0)));
    parties.put(2, new Party(2, "localhost", ports.get(1)));    
    Thread t1 = new Thread(new Runnable() {
      
      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
        KryoNetNetwork network = null;
        try {
          network = new KryoNetNetwork(conf);
          Assert.fail("Should not be able to connect");
        } catch (RuntimeException e) { 
          //ignore
        } finally {
          try {
            if (network != null) {
              network.close();
            }
          } catch (IOException e) {
            Assert.fail("Should be able to close the network if it exists");
          }
        }
      }
    });
    
    t1.start();
    try {      
      t1.join(200);
      t1.interrupt();
      t1.join();
    } catch (InterruptedException e) {
      Assert.fail("Threads should finish without main getting interrupted");
    }    
  }
  
  @Test
  public void testPartiesReconnect() {
    testMultiplePartiesReconnect(2, 100);
    testMultiplePartiesReconnect(3, 50);
    testMultiplePartiesReconnect(5, 50);
  }
  
  private void testMultiplePartiesReconnect(int noOfParties, int noOfRetries) {    
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      parties.put(i, new Party(i, "localhost", ports.get(i - 1)));
    }    
    for (int retry = 0; retry < noOfRetries; retry++) {
      List<Thread> threads = new ArrayList<>();      
      final List<Exception> exs = new ArrayList<>();
      final ConcurrentHashMap<Integer, KryoNetNetwork> networks = new ConcurrentHashMap<>();
      
      for (int i = 0; i < noOfParties; i++) {
        final int myId = i + 1;        
        final Thread t = new Thread(new Runnable() {
          
          @Override
          public void run() {
            NetworkConfiguration conf = new NetworkConfigurationImpl(myId, parties);            
            try {
              KryoNetNetwork network = new KryoNetNetwork(conf);
              networks.put(myId, network);
            } catch (RuntimeException e) {
              exs.add(e);
              Assert.fail();
            }            
          }
        });
        threads.add(t);
        t.start();
      }
      
      for (Thread t : threads) {
        try {
          t.join();
        } catch (InterruptedException e) {
          Assert.fail("Threads should finish without main getting interrupted");
        }
      }
      if (exs.size() != 0) {
        Assert.fail("Exception occured while trying to connect: " + exs.get(0).getMessage());
      }
      //disconnect again.
      for (KryoNetNetwork network : networks.values()) {
        try {
          network.close();
        } catch (IOException e) {
          Assert.fail("Should be able to disconnect again");
        }
      }      
    }
  }
}
