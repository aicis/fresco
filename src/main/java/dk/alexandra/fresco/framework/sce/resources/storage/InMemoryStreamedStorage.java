package dk.alexandra.fresco.framework.sce.resources.storage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import dk.alexandra.fresco.framework.MPCException;

public class InMemoryStreamedStorage implements StreamedStorage {

	private Storage storage;
	private Map<String, Queue<Serializable>> queues;
	
	public InMemoryStreamedStorage(Storage storage) {
		this.storage = storage;
		this.queues = new HashMap<String, Queue<Serializable>>();
	}
	
	@Override
	public boolean putObject(String name, String key, Serializable o) {
		return this.storage.putObject(name, key, o);
	}

	@Override
	public <T extends Serializable> T getObject(String name, String key) {
		return this.storage.getObject(name, key);
	}

	@Override
	public boolean removeFromStorage(String name, String key) {
		return this.storage.removeFromStorage(name, key);
	}

	@Override
	public boolean removeNameFromStorage(String name) {
		return this.storage.removeNameFromStorage(name);
	}

	@Override
	public <T extends Serializable> T getNext(String name) {
		Queue<Serializable> queue = this.queues.get(name);
		if(queue == null) {
			throw new MPCException("Could not find any store with name "+name);
		}
		Serializable res = queue.poll();
		if(res == null) {
			throw new MPCException("No more elements in store with name "+name);
		}
		return (T)res;
	}

	@Override
	public boolean putNext(String name, Serializable o) {
		if(this.queues.containsKey(name)) {
			this.queues.get(name).offer(o);
			return true;
		} else {
			Queue<Serializable> queue = new LinkedBlockingQueue<>();
			queue.offer(o);
			this.queues.put(name, queue);
			return true;
		}
	}

	@Override
	public void shutdown() {
		
	}

}
