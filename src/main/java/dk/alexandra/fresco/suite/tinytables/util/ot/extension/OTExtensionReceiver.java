package dk.alexandra.fresco.suite.tinytables.util.ot.extension;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTReceiver;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTSigma;

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
public class OTExtensionReceiver implements OTReceiver {

	private InetSocketAddress address;

	public OTExtensionReceiver(InetSocketAddress address) {
		this.address = address;
	}
	
	@Override
	public boolean[] receive(OTSigma[] sigmas) {
		return transfer(sigmas);
	}
	
	private boolean[] transfer(OTSigma[] sigmas) {
		// As default we run it in a seperate process
		return transfer(sigmas, true);
	}

	private boolean[] transfer(OTSigma[] sigmas, boolean seperateProcess) {

		try {

			/*
			 * There is an upper bound on how big a terminal cmd can be, so if
			 * we have too many OT's we need to do them a batch at a time.
			 */
			if (seperateProcess && sigmas.length > OTExtensionConfig.MAX_OTS) {
				boolean[] out1 = transfer(Arrays.copyOfRange(sigmas, 0, OTExtensionConfig.MAX_OTS));
				boolean[] out2 = transfer(Arrays.copyOfRange(sigmas, OTExtensionConfig.MAX_OTS, sigmas.length));

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
				if (seperateProcess) {
					/*
					 * We run the OT in a seperate process. Here we call the
					 * main method of NativeOTReceiver. This is required when
					 * running the JUnit tests where the players are on seperate
					 * threads in the same JVM.
					 */
					String base64sigmas = Base64.getEncoder().encodeToString(binarySigmas);

					ProcessBuilder builder = new ProcessBuilder(OTExtensionConfig.SCAPI_CMD, "-cp",
							OTExtensionConfig.CLASSPATH, OTExtensionConfig.OT_RECEIVER, address.getHostName(),
							Integer.toString(address.getPort()), base64sigmas);
					builder.directory(new File(OTExtensionConfig.PATH));

					Process p = builder.start();

					p.waitFor();

					String base64output = new BufferedReader(new InputStreamReader(
							p.getInputStream())).lines().collect(Collectors.joining("\n"));

					binaryOutput = Base64.getDecoder().decode(base64output);
				} else {
					// TODO: Not usable in JUnit tests so this has NOT been
					// tested yet.
					throw new UnsupportedOperationException("Not implemented yet");
					// binaryOutput = NativeOTReceiver.receive(host, port,
					// binarySigmas);
				}

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
