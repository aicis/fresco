package dk.alexandra.fresco.suite.tinytables.util.ot.extension;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

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
	public void send(List<OTInput> inputs) {

		if (inputs.size() > OTExtensionConfig.MAX_OTS) {
			send(inputs.subList(0, OTExtensionConfig.MAX_OTS));
			send(inputs.subList(OTExtensionConfig.MAX_OTS, inputs.size()));
		} else {

			byte[] input0 = Encoding.encodeBooleans(OTInput.getAll(inputs, 0));
			String base64input0 = Base64.getEncoder().encodeToString(input0);

			byte[] input1 = Encoding.encodeBooleans(OTInput.getAll(inputs, 1));
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
