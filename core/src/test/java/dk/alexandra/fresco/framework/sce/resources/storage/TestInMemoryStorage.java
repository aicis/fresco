package dk.alexandra.fresco.framework.sce.resources.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;


public class TestInMemoryStorage {

  InMemoryStorage memStore;

  @Before
  public void setup() {
    memStore = new InMemoryStorage();
  }

  @Test
  public void testPutAndGetObjects() {
    memStore.putObject("TestStore", "Key1", "Foo");
    assertEquals("Foo", memStore.getObject("TestStore", "Key1"));
    memStore.putObject("TestStore", "Key2", "Bar");
    assertEquals("Bar", memStore.getObject("TestStore", "Key2"));
    memStore.putObject("TestStore", "Key2", "Baz");
    assertEquals("Baz", memStore.getObject("TestStore", "Key2"));
    memStore.putObject("TestStore2", "Key2", "Fum");
    assertEquals("Fum", memStore.getObject("TestStore2", "Key2"));
    assertNull(memStore.getObject("NonExistentStoreName", "Key"));
    assertNull(memStore.getObject("TestStore", "NonExistentKey"));
  }

}
