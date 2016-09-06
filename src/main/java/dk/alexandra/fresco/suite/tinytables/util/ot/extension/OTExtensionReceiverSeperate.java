package dk.alexandra.fresco.suite.tinytables.util.ot.extension;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTReceiver;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTSigma;
import edu.biu.scapi.comm.Party;

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
public class OTExtensionReceiverSeperate implements OTReceiver {

	private Party party;

	/**
	 * Create a new OTExtensionReceiverSeperate which will perform OTExtension
	 * with a sender running at the address and port as specified in the
	 * <code>party</code> parameter.
	 * 
	 * @param party
	 */
	public OTExtensionReceiverSeperate(Party party) {
		this.party = party;
	}

	@Override
	public boolean[] receive(OTSigma[] sigmas) {
		return transfer(sigmas);
	}

	private boolean[] transfer(OTSigma[] sigmas) {
		// As default we run it in a seperate process
		return transfer(sigmas, false);
	}

	private boolean[] transfer(OTSigma[] sigmas, boolean seperateProcess) {

		try {

			/*
			 * There is an upper bound on how big a terminal cmd can be, so if
			 * we have too many OT's we need to do them a batch at a time.
			 */
			if (sigmas.length > OTExtensionConfig.MAX_OTS) {
				boolean[] out1 = transfer(Arrays.copyOfRange(sigmas, 0, OTExtensionConfig.MAX_OTS));
				boolean[] out2 = transfer(Arrays.copyOfRange(sigmas, OTExtensionConfig.MAX_OTS,
						sigmas.length));

				boolean[] out = new boolean[out1.length + out2.length];
				System.arraycopy(out1, 0, out, 0, out1.length);
				System.arraycopy(out2, 0, out, out1.length, out2.length);
				return out;
			} else {
				byte[] binarySigmas = new byte[sigmas.length];
				for (int i = 0; i < binarySigmas.length; i++) {
					binarySigmas[i] = Encoding.encodeBoolean(sigmas[i].getSigma());
				}

				byte[] binaryOutput = null;
				/*
				 * We run the OT in a seperate process. Here we call the main
				 * method of NativeOTReceiver. This is required when running the
				 * JUnit tests where the players are on seperate threads in the
				 * same JVM.
				 */
				String base64sigmas = Base64.getEncoder().encodeToString(binarySigmas);

				ProcessBuilder builder = new ProcessBuilder(OTExtensionConfig.SCAPI_CMD, "-cp",
						OTExtensionConfig.CLASSPATH, OTExtensionConfig.OT_RECEIVER,
						party.getIpAddress().getHostName(), Integer.toString(party.getPort()), base64sigmas);
				builder.directory(new File(OTExtensionConfig.PATH));

				Process p = builder.start();

				p.waitFor();

				String base64output = new BufferedReader(new InputStreamReader(p.getInputStream()))
						.lines().collect(Collectors.joining("\n"));

				binaryOutput = Base64.getDecoder().decode(base64output);

				// Decode output to booleans
				boolean[] output = Encoding.decodeBooleans(binaryOutput);
				return output;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
