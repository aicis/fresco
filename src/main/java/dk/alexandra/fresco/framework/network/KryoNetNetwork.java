package dk.alexandra.fresco.framework.network;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;

public class KryoNetNetwork implements Network {

	private final Map<Integer, Server> servers;
	private final Map<Integer, Client> clients;
	private final NetworkConfiguration conf;
	private final Map<Integer, BlockingQueue<Object>> queues;

	public KryoNetNetwork(NetworkConfiguration conf) {
		//Log.set(Log.LEVEL_DEBUG);
		
		this.conf = conf;		
		this.clients = new HashMap<>();
		this.servers = new HashMap<>();
		for(int i = 1; i <= conf.noOfParties(); i++) {
			if(conf.getMyId() != i) {
				Server server = new Server();
				Registrator.register(server);
				this.servers.put(i, server);
			}
		}				
		this.queues = new HashMap<>();
		for (int i = 1; i <= conf.noOfParties(); i++) {			
			if (i != conf.getMyId()) {
				Client client = new Client();				
				Registrator.register(client);				
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
			//Maybe a keep alive message will be offered to the queue. - so we should ignore it. 
			if(!(object instanceof FrameworkMessage.KeepAlive)) {
				queue.offer(object);	
			}			
		}
	}
	
	@Override
	public void connect(int timeoutMillis) throws IOException {
		final Semaphore semaphore = new Semaphore(-(conf.noOfParties()-2));
		int inc = 0;
		for (int i = 1; i <= conf.noOfParties(); i++) {
			if (i != conf.getMyId()) {
				Server server = this.servers.get(i);
				try {
					server.bind(conf.getMe().getPort() + inc++);
					server.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				server.addListener(new NaiveListener(queues.get(i)));				
				
				Client client = clients.get(i);

				client.addListener(new Listener(){
					@Override
					public void connected (Connection connection) {
						System.out.println("Client connected. Yea!");
						semaphore.release();
					}
				});
				
				client.start();
				
				
				String hostname = conf.getParty(i).getHostname();
				int port = conf.getParty(i).getPort(); 
				new Thread("Connect") {
					public void run () {
						boolean success = false;
						while(!success) {
							try {							
								client.connect(5000, hostname, port);
								// Server communication after connection can go here, or in Listener#connected().
								success = true;
							} catch (IOException ex) {								
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									throw new RuntimeException("Thread got interrupted while trying to reconnect.");
								}
							}
						}
					}
				}.start();				
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
		for (int i = 1; i <= conf.noOfParties(); i++) {
			if (i != conf.getMyId()) {
				this.clients.get(i).close();
				this.servers.get(i).close();
			}
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
