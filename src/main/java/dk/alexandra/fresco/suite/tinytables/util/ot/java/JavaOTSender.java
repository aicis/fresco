package dk.alexandra.fresco.suite.tinytables.util.ot.java;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTSender;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTInput;
import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.OTBatchOnByteArraySInput;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.semiHonest.OTSemiHonestDDHBatchOnByteArraySender;

public class JavaOTSender implements OTSender {

	private Network network;
	private int myId;

	public JavaOTSender(Network network, int myId) {
		this.network = network;
		this.myId = myId;
	}
	
	@Override
	public void send(OTInput[] inputs) {
		
		ArrayList<byte[]> x0 = new ArrayList<byte[]>();
		ArrayList<byte[]> x1 = new ArrayList<byte[]>();
		for (int i = 0; i < inputs.length; i++) {
			x0.add(new byte[] {Encoding.encodeBoolean(inputs[i].getX0())});
			x1.add(new byte[] {Encoding.encodeBoolean(inputs[i].getX1())});
		}
		OTBatchOnByteArraySInput otsInputs = new OTBatchOnByteArraySInput(x0, x1);
		OTSemiHonestDDHBatchOnByteArraySender sender = new OTSemiHonestDDHBatchOnByteArraySender();
		
		try {
			sender.transfer(new Channel() {

				@Override
				public void close() {
					
				}

				@Override
				public boolean isClosed() {
					return false;
				}

				@Override
				public Serializable receive() throws ClassNotFoundException, IOException {
					return network.receive("0", otherPlayerId());
				}

				@Override
				public void send(Serializable otInputs) throws IOException {
					network.send("0", otherPlayerId(), otInputs);
				}
				
			}, otsInputs);
		} catch (ClassNotFoundException | IOException e) {
			// Do nothing
		}
		
	}

	private int otherPlayerId() {
		return myId == 1 ? 2 : 1;
	}

	
}
