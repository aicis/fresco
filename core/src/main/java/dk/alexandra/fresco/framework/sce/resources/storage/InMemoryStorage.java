package dk.alexandra.fresco.framework.sce.resources.storage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class InMemoryStorage implements Storage {

  private Map<String, Map<String, Serializable>> objects;

  public InMemoryStorage() {
    this.objects = new HashMap<>();
  }

  @Override
  public boolean putObject(String name, String key, Serializable o) {
    Map<String, Serializable> table = this.objects.get(name);
    if (table == null) {
      this.objects.put(name, new HashMap<String, Serializable>());
      table = this.objects.get(name);
    }
    table.put(key, o);
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Serializable> T getObject(String name, String key) {
    Map<String, Serializable> table = this.objects.get(name);
    if (table == null) {
      return null;
    }
    return (T) table.get(key);
  }

}
