package dk.alexandra.fresco.suite.tinytables.util.ot.scapi;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Base64;

import edu.biu.scapi.comm.Party;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.otExtension.OTExtensionGeneralSInput;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.otExtension.OTSemiHonestExtensionSender;

public class OtSender {

	public static void main(String[] args) throws UnknownHostException {
		// System.loadLibrary("OtExtensionJavaInterface");	
		
		String host = args[0];
		String portAsString = args[1];
		String input0Base64 = args[2];
		String input1Base64 = args[3];

		int port;
		try {
			port = Integer.parseInt(portAsString);
		} catch (NumberFormatException e) {
			System.err.println("Invalid port number: " + portAsString);
			System.exit(-1);
			return;
		}
		Party party = new Party(InetAddress.getByName(host), port);
		OTSemiHonestExtensionSender sender = new OTSemiHonestExtensionSender(party);
		
		byte[] input0, input1;
		try {
			input0 = Base64.getDecoder().decode(input0Base64);
			input1 = Base64.getDecoder().decode(input1Base64);
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid arrays: " + input0Base64 + ", " + input1Base64);
			System.exit(-1);
			return;
		}
		
		if (input0.length != input1.length) {
			System.err.println("Length of input arrays must match, " + input0.length + "!=" + input1.length);
		}
		int n = input0.length;
		
		OTExtensionGeneralSInput input = new OTExtensionGeneralSInput(input0, input1, n);
		sender.transfer(null, input);
	}

}
