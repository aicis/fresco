package dk.alexandra.fresco.suite.tinytables.util.ot.extension;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTSender;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTInput;
import edu.biu.scapi.comm.Party;

public class OTExtensionSenderSeperate implements OTSender {

	private Party party;

	/**
	 * Create a new OTExtensionSenderSeperate running on the specified address
	 * and port.
	 * 
	 * @param party
	 */
	public OTExtensionSenderSeperate(Party party) {
		this.party = party;
	}

	@Override
	public void send(OTInput[] inputs) {

		if (inputs.length > OTExtensionConfig.MAX_OTS) {
			send(Arrays.copyOfRange(inputs, 0, OTExtensionConfig.MAX_OTS));
			send(Arrays.copyOfRange(inputs, OTExtensionConfig.MAX_OTS, inputs.length));
		} else {

			byte[] input0 = new byte[inputs.length];
			byte[] input1 = new byte[inputs.length];

			for (int i = 0; i < inputs.length; i++) {
				input0[i] = Encoding.encodeBoolean(inputs[i].getX0());
				input1[i] = Encoding.encodeBoolean(inputs[i].getX1());
			}

			String base64input0 = Base64.getEncoder().encodeToString(input0);
			String base64input1 = Base64.getEncoder().encodeToString(input1);

			ProcessBuilder builder = new ProcessBuilder(OTExtensionConfig.SCAPI_CMD, "-cp",
					OTExtensionConfig.CLASSPATH, OTExtensionConfig.OT_SENDER,
					party.getIpAddress().getHostName(), Integer.toString(party.getPort()), base64input0,
					base64input1);

			String path = new File("") + OTExtensionConfig.PATH;

			builder.directory(new File(path));
			Process p;
			try {
				p = builder.start();
				p.waitFor();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
