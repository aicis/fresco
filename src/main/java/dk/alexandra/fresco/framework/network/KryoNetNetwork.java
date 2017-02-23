package dk.alexandra.fresco.framework.network;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;

public class KryoNetNetwork implements Network {

	private final Server server;
	private final Map<Integer, Client> clients;
	private final NetworkConfiguration conf;
	private final Map<Integer, BlockingQueue<Object>> queues;

	public KryoNetNetwork(NetworkConfiguration conf) {
		this.conf = conf;
		this.server = new Server();
		Registrator.register(server);
		this.clients = new HashMap<>();
		try {
			this.server.bind(conf.getMe().getPort());
			this.server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.queues = new HashMap<>();
		for (int i = 1; i <= conf.noOfParties(); i++) {
			Client client = new Client();
			Registrator.register(client);
			if (i != conf.getMyId()) {
				clients.put(i, client);				
			}
			this.queues.put(i, new ArrayBlockingQueue<>(1000));
		}

	}

	private class NaiveListener extends Listener {

		private BlockingQueue<Object> queue;
		
		public NaiveListener(BlockingQueue<Object> queue) {
			this.queue = queue;
		}

		@Override
		public void received(Connection connection, Object object) {
			queue.offer(object);
		}
	}

	@Override
	public void connect(int timeoutMillis) throws IOException {
		final Semaphore semaphore = new Semaphore(-(conf.noOfParties()-2));
		for (int i = 1; i <= conf.noOfParties(); i++) {
			if (i != conf.getMyId()) {
				Client client = clients.get(i);				
				client.start();
				
				client.addListener(new Listener(){
					@Override
					public void connected (Connection connection) {
						semaphore.release();
					}
				});
				
				String hostname = conf.getParty(i).getHostname();
				int port = conf.getParty(i).getPort(); 
				new Thread("Connect") {
					public void run () {
						try {
							client.connect(5000, hostname, port);
							// Server communication after connection can go here, or in Listener#connected().
						} catch (IOException ex) {
							ex.printStackTrace();
							System.exit(1);
						}
					}
				}.start();
				this.server.addListener(new NaiveListener(queues.get(i)));
			}
		}
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void send(String channel, int partyId, Serializable data) throws IOException {
		if(this.conf.getMyId() == partyId) {
			try {
				this.queues.get(partyId).put(data);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {		
			this.clients.get(partyId).sendTCP(data);
		}
	}

	@Override
	public <T extends Serializable> T receive(String channel, int partyId) throws IOException {
		try {
			return (T) this.queues.get(partyId).take();
		} catch (InterruptedException e) {
			throw new MPCException("receive got interrupted");
		}
	}

	@Override
	public void close() throws IOException {
		this.server.close();
		for (int i = 1; i <= conf.noOfParties(); i++) {
			this.clients.get(i).close();
		}
	}
	
	private static class Registrator {
		
		public static void register(EndPoint endpoint) {
			Kryo kryo = endpoint.getKryo();
			kryo.register(byte[].class);
			kryo.register(BigInteger.class);
			kryo.register(BigInteger[].class);
			kryo.register(Serializable.class);
			kryo.register(Serializable[].class);
		}
	}
}
