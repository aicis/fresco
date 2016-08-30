package dk.alexandra.fresco.suite.tinytables.util.ot.extension;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Base64;

import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTInput;
import edu.biu.scapi.comm.Party;

public class OTExtensionSenderApplication {

	public static void main(String[] args) throws UnknownHostException {
		
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
		byte[] input0, input1;
		try {
			input0 = Base64.getDecoder().decode(input0Base64);
			input1 = Base64.getDecoder().decode(input1Base64);
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid arrays: " + input0Base64 + ", " + input1Base64);
			System.exit(-1);
			return;
		}
		
		send(host, port, input0, input1);
	}
	
	public static void send(String host, int port, byte[] input0, byte[] input1) throws UnknownHostException {
		OTExtensionSender sender = new OTExtensionSender(new Party(InetAddress.getByName(host), port));
		OTInput[] otInputs = new OTInput[input0.length];
		for (int i = 0; i < otInputs.length; i++) {
			otInputs[i] = new OTInput(Encoding.decodeBoolean(input0[i]), Encoding.decodeBoolean(input1[i]));
		}
		sender.send(otInputs);
	}

}
