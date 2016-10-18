package dk.alexandra.fresco.suite.tinytables.util.ot.extension.seperate;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.List;

import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTSigma;
import dk.alexandra.fresco.suite.tinytables.util.ot.extension.OTExtensionReceiver;
import edu.biu.scapi.comm.Party;

/**
 * Perform Oblivious transfer extension as the receiving part. Should be called
 * with three parameters: The host address of the sender, the port number and the sigmas as a
 * Base64-encoded byte-array with 0x00 = false and 0x01 = true.
 * 
 * @author jonas
 *
 */
public class OTExtensionReceiverApplication {

	public static void main(String[] args) throws UnknownHostException {

		String host = args[0];
		String portAsString = args[1];
		String sigmasBase64 = args[2];

		int port;
		try {
			port = Integer.parseInt(portAsString);
		} catch (NumberFormatException e) {
			System.out.println("Invalid port number: " + portAsString);
			System.exit(-1);
			return;
		}
		
		byte[] sigmas;
		try {
			sigmas = Base64.getDecoder().decode(sigmasBase64);
		} catch (IllegalArgumentException e) {
			System.out.println("Invalid sigmas array: " + sigmasBase64);
			System.exit(-1);
			return;
		}
		byte[] output = receive(host, port, sigmas);
		String outputBase64 = Base64.getEncoder().encodeToString(output);
		System.out.println(outputBase64);
	}
	
	public static byte[] receive(String host, int port, byte[] sigmas) throws UnknownHostException {
		
		OTExtensionReceiver receiver = new OTExtensionReceiver(new Party(InetAddress.getByName(host), port));
		List<OTSigma> otSigmas = OTSigma.fromList(Encoding.decodeBooleans(sigmas));
		List<Boolean> outputs = receiver.receive(otSigmas);
				
		return Encoding.encodeBooleans(outputs);
	}

}
