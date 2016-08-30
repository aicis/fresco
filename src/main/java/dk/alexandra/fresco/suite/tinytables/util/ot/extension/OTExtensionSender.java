package dk.alexandra.fresco.suite.tinytables.util.ot.extension;

import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTSender;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTInput;
import edu.biu.scapi.comm.Party;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.otExtension.OTExtensionGeneralSInput;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.otExtension.OTSemiHonestExtensionSender;

/**
 * We use SCAPI's OT Extension library for doing oblivious transfers. However,
 * this lib is implemented in C++ and we need to call it using JNI. Also, for
 * testing we run the different players on the same machine in two different
 * threads which seem to cause problems with the lib, so to avoid that we run
 * the JNI in seperate processes.
 * 
 * @author jonas
 *
 */
public class OTExtensionSender implements OTSender {

	private Party party;

	public OTExtensionSender(Party party) {
		this.party = party;
	}

	@Override
	public void send(OTInput[] inputs) {
		byte[] input0 = new byte[inputs.length];
		byte[] input1 = new byte[inputs.length];

		for (int i = 0; i < inputs.length; i++) {
			input0[i] = Encoding.encodeBoolean(inputs[i].getX0());
			input1[i] = Encoding.encodeBoolean(inputs[i].getX1());
		}

		OTSemiHonestExtensionSender sender = new OTSemiHonestExtensionSender(party);
		OTExtensionGeneralSInput input = new OTExtensionGeneralSInput(input0, input1, input0.length);
		sender.transfer(null, input);
	}

}
