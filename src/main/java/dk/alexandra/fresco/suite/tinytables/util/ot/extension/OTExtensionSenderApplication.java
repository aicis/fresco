package dk.alexandra.fresco.suite.tinytables.util.ot.extension;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.List;

import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTInput;
import edu.biu.scapi.comm.Party;

/**
 * Perform Oblivious transfer extension as the sending part. Should be called
 * with three parameters: The host address of the sender (this party), the port
 * number and the sigmas as a Base64-encoded byte-array with 0x00 = false and
 * 0x01 = true.
 * 
 * @author jonas
 *
 */
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
		List<OTInput> otInputs = OTInput.fromLists(Encoding.decodeBooleans(input0),
				Encoding.decodeBooleans(input1));
		sender.send(otInputs);
	}

}
