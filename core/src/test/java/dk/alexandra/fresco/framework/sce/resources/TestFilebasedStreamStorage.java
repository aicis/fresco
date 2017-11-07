package dk.alexandra.fresco.framework.sce.resources;

import java.io.Serializable;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.exceptions.NoMoreElementsException;

public class TestFilebasedStreamStorage {

  StreamedStorage storage = new FilebasedStreamedStorageImpl(new InMemoryStorage());
  
  @Test
  public void testPutAndGetObject(){
    String myObj = "This is a test";
    storage.putObject("test-obj", "test-key", myObj);
    Serializable s = storage.getObject("test-obj", "test-key");
    storage.shutdown();
    Assert.assertThat(s, Is.is(myObj));
  }

  
  @Test
  public void testPutAndGetNext() throws NoMoreElementsException{
    String myObj = "This is a test";
    boolean success = storage.putNext("test-obj", myObj);
    Assert.assertTrue(success);
    Serializable s = storage.getNext("test-obj");
    storage.shutdown();
    Assert.assertThat(s, Is.is(myObj));
  }
  
  @Test(expected = MPCException.class)
  public void testGetNonExisting() throws NoMoreElementsException {
    storage.getNext("test-obj-2");
    Assert.fail();    
  }

  @Test(expected = MPCException.class)
  public void testPutBadName() throws NoMoreElementsException {
    storage.putNext("\0", "data");
    Assert.fail();    
  }
  
  @Test(expected = NoMoreElementsException.class)
  public void testMultipleReadWrites() throws NoMoreElementsException{
    String myObj = "This is a test";
    boolean success = storage.putNext("test-obj", myObj);
    Assert.assertTrue(success);
    success = storage.putNext("test-obj", "This-is-second-test");
    Assert.assertTrue(success);
    Serializable s = storage.getNext("test-obj");
    Assert.assertThat(s, Is.is(myObj));
    s = storage.getNext("test-obj");
    Assert.assertThat(s, Is.is("This-is-second-test"));
    storage.getNext("test-obj");
    
    Assert.fail();
  }


  
}
