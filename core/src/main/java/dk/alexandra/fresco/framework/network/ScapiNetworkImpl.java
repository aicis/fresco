/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.framework.network;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.util.Base64;
import edu.biu.scapi.comm.AuthenticatedChannel;
import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.comm.EncryptedChannel;
import edu.biu.scapi.comm.PlainChannel;
import edu.biu.scapi.comm.multiPartyComm.MultipartyCommunicationSetup;
import edu.biu.scapi.comm.multiPartyComm.SocketMultipartyCommunicationSetup;
import edu.biu.scapi.comm.twoPartyComm.PartyData;
import edu.biu.scapi.comm.twoPartyComm.SocketPartyData;
import edu.biu.scapi.exceptions.SecurityLevelException;
import edu.biu.scapi.midLayer.symmetricCrypto.encryption.ScCTREncRandomIV;
import edu.biu.scapi.midLayer.symmetricCrypto.encryption.ScEncryptThenMac;
import edu.biu.scapi.midLayer.symmetricCrypto.mac.Mac;
import edu.biu.scapi.midLayer.symmetricCrypto.mac.ScCbcMacPrepending;
import edu.biu.scapi.primitives.prf.AES;
import edu.biu.scapi.primitives.prf.bc.BcAES;

/**
 * Network based on SCAPI network layer.
 *
 * For now it only uses non-encrypted socket-based communication.
 *
 */
public class ScapiNetworkImpl implements Network {

	private NetworkConfiguration conf;

	// Unless explicitly named, SCAPI channels are named with
	// strings "0", "1", etc.
	private int defaultChannel = 0;

	// List of all parties, with this party as first entry.
	private List<PartyData> parties;

	private Map<PartyData, Map<String, Channel>> connections;
	private Map<Integer, PartyData> idToPartyData;
	private int channelAmount;

	//Queue for self-sending
	private Map<Integer, BlockingQueue<Serializable>> queues;

	/**
	 * 
	 * @param conf - The configuration with info about whom to connect to.
	 * @param channelAmount The amount of channels each player needs to each other.
	 */
	public ScapiNetworkImpl() {		 
	}
	
	@Override
	public void init(NetworkConfiguration conf, int channelAmount) {
		this.channelAmount = channelAmount;
		this.conf = conf;
	}

	//TODO: Include player to integer map to indicate 
	//how many channels are wanted to each player.
	//Implement this also for send to self queues.
	public void connect(int timeoutMillis) throws IOException {
		// Convert FRESCO configuration to SCAPI configuration.
		parties = new LinkedList<PartyData>();
		idToPartyData = new HashMap<Integer, PartyData>();
		List<String> sharedSecretKeys = new LinkedList<String>();
		System.out.println(conf);
		for (int id = 1; id <= conf.noOfParties(); id++) {
			Party frescoParty = conf.getParty(id);
			String iadrStr = frescoParty.getHostname();
			InetAddress iadr = InetAddress.getByName(iadrStr);
			int port = frescoParty.getPort();
			SocketPartyData scapyParty = new SocketPartyData(iadr, port);
			parties.add(scapyParty);
			sharedSecretKeys.add(frescoParty.getSecretSharedKey());
			idToPartyData.put(id, scapyParty);
		}
		// SCAPI requires party itself to be first in list.
		Collections.swap(parties, 0, conf.getMyId() - 1);
		Collections.swap(sharedSecretKeys, 0, conf.getMyId() - 1);
		
		List<PartyData> others = new LinkedList<PartyData>(parties);
		others.remove(0);
		// Create the communication setup class.
		MultipartyCommunicationSetup commSetup = new SocketMultipartyCommunicationSetup(parties);
		// Request one channel between me and each other party.
		HashMap<PartyData, Object> connectionsPerParty = new HashMap<PartyData, Object>(
				others.size());
		//queue to self
		this.queues = new HashMap<Integer, BlockingQueue<Serializable>>();
		for (int i = 0; i < others.size(); i++) {
			connectionsPerParty.put(others.get(i), this.channelAmount);
		}

		for(int i = 0; i < this.channelAmount; i++) {
			//TODO: figure out correct number for capacity.
			this.queues.put(i, new ArrayBlockingQueue<Serializable>(10000));
		}

		try {
			connections = commSetup.prepareForCommunication(connectionsPerParty, timeoutMillis);
		} catch (TimeoutException e) {
			throw new IOException(e);
		}
		
		// Enable secure (auth + encrypted) channels if a key is specified.
		for (int id = 0; id < sharedSecretKeys.size(); id++) {
			int partyId = id + 1;
			if(this.conf.getMyId() != partyId && sharedSecretKeys.get(id) != null) {
				Reporter.config("Using authentication and encryption for channel(s) to party " + partyId);
				for(int i = 0; i < this.channelAmount; i++) {
					PartyData pd = idToPartyData.get(partyId);
					Map<String, Channel> channels = connections.get(pd);
					String cStr = "" + i;
					PlainChannel c = (PlainChannel)channels.get(cStr);
					Channel secureChannel;
					String sharedSecretKey = sharedSecretKeys.get(id); 
					try {
						secureChannel = getSecureChannel(c, sharedSecretKey);
					} catch (InvalidKeyException e) {
						throw new MPCException("Invalid AES key (shared secret key): " + sharedSecretKey, e);
					} catch (SecurityLevelException e) {
						throw new MPCException("SCAPI security level exception when creating channel " + cStr + " towards " + partyId, e);
					}
				
					channels.put(cStr, secureChannel);
				}
			}
		}
		
	}

	// We currently either use plain channels or auth+enc channels. Future
	// version may allow only auth.
	@SuppressWarnings("unused")
	private AuthenticatedChannel getAuthenticatedChannel(PlainChannel channel, String base64EncodedSSKey)
			throws SecurityLevelException, InvalidKeyException {
		Mac mac = new ScCbcMacPrepending(new BcAES());
		byte[] aesFixedKey = Base64.decodeFromString(base64EncodedSSKey);
		SecretKey key = new SecretKeySpec(aesFixedKey, "AES");
		mac.setKey(key);
		AuthenticatedChannel authedChannel = new AuthenticatedChannel(channel, mac);
		return authedChannel;
	}

	private EncryptedChannel getSecureChannel(PlainChannel ch, String base64EncodedSSKey) throws InvalidKeyException, SecurityLevelException {
		byte[] aesFixedKey = Base64.decodeFromString(base64EncodedSSKey);
		SecretKey aesKey = new SecretKeySpec(aesFixedKey, "AES");
		AES encryptAes = new BcAES();
		encryptAes.setKey(aesKey);
		ScCTREncRandomIV enc = new ScCTREncRandomIV(encryptAes);
		AES macAes = new BcAES();
		macAes.setKey(aesKey);
		ScCbcMacPrepending cbcMac = new ScCbcMacPrepending(macAes);
		ScEncryptThenMac encThenMac = new ScEncryptThenMac(enc, cbcMac);
		EncryptedChannel secureChannel = new EncryptedChannel(ch, encThenMac);
		return secureChannel;
	}
	

	/**
	 * Close all channels to other parties.
	 * 
	 */
	public void close() throws IOException {
		if(connections != null) {
			for (Map<String, Channel> m : connections.values()) {
				for (Channel c : m.values()) {
					c.close();
				}
			}
		}
	}

	/**
	 * Send using default channel (0).
	 * 
	 * TODO: When writing to TCP socket, does message always (1) get send eventually, or (2) does it
	 * risk getting buffered indefinitely (until explicitly calling flush/close)?
	 * 
	 * TODO: There is also potential deadlock with TCP if both parties send large buffers to
	 * each other simultaneously.
	 * 
	 * @param receiverId Non-negative id of player to receive data.
	 * @param data
	 * @throws IOException 
	 * 
	 */
	public void send(int receiverId, byte[] data) throws IOException {
		send(defaultChannel, receiverId, data);
	}


	/**
	 * Receive data using default channel (0).
	 * 
	 * @param id Non-negative id of player from which to receive data.
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * 
	 */
	public byte[] receive(int id) throws IOException{
		return receive(defaultChannel, id);
	}

	public void send(int channel, Map<Integer, byte[]> output) throws IOException {
		for(int playerId: output.keySet()) {
			this.send(channel, playerId, output.get(playerId));
		}
	}

	public Map<Integer, Serializable> receive(int channel, Set<Integer> expectedInputForNextRound) throws IOException {
		//TODO: Maybe use threading for each player
		Map<Integer, Serializable> res = new HashMap<Integer, Serializable>();
		for(int i : expectedInputForNextRound){
			byte[] r = this.receive(channel, i);
			res.put(i, r);
		}
		return res;
	}

	public int getMyId() {
		return this.conf.getMyId();
	}

	public int getNoParties() {
		return this.conf.noOfParties();
	}

	@Override
	public void send(int channel, int partyId, byte[] data)
			throws IOException {
		if(partyId == this.conf.getMyId()) {
			this.queues.get(channel).add(data);
			return;
		}
		if (!idToPartyData.containsKey(partyId)) {
			throw new MPCException("No party with id " + partyId);
		}
		PartyData receiver = idToPartyData.get(partyId);
		Map<String,Channel> channels = connections.get(receiver);
		Channel c = channels.get(""+channel);
		c.send(data);		
	}

	@Override
	public byte[] receive(int channel, int partyId) throws IOException {
		if(partyId == this.conf.getMyId()) {
			byte[] res = (byte[]) this.queues.get(channel).poll();
			if(res == null){
				throw new MPCException("Self(" + partyId + ") have not send anything on channel " + channel + "before receive was called.");
			}
			return res;
		} else {
			PartyData receiver = idToPartyData.get(partyId);
			Map<String,Channel> channels = connections.get(receiver);
			Channel c = channels.get(""+channel);
			if (c == null) {
				throw new MPCException(
						"Trying to send via channel " + channel + ", but this network was initiated with only " + this.channelAmount + " channels.");
			}
			byte[] res = null;
			try {
				res = (byte[]) c.receive();
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Weird class not found exception, sry. ", e);
			}			
			return res;
		}
	}

}
