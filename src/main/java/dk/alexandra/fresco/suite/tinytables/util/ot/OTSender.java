package dk.alexandra.fresco.suite.tinytables.util.ot;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTInput;

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
public class OTSender {

	public static void transfer(String host, int port, OTInput[] inputs) {
		// As default we run it in a seperate process
		transfer(host, port, inputs, true);
	}

	public static void transfer(String host, int port, OTInput[] inputs, boolean seperateProcess) {

		try {

			/*
			 * There is an upper bound on how big a terminal cmd can be, so if
			 * we have too many OT's we need to do them a batch at a time.
			 */
			if (seperateProcess && inputs.length > OTConfig.MAX_OTS) {
				transfer(host, port, Arrays.copyOfRange(inputs, 0, OTConfig.MAX_OTS));
				transfer(host, port, Arrays.copyOfRange(inputs, OTConfig.MAX_OTS, inputs.length));
			} else {
				
				byte[] input0 = new byte[inputs.length];
				byte[] input1 = new byte[inputs.length];
				
				for (int i = 0; i < inputs.length; i++) {
					input0[i] = Encoding.encodeBoolean(inputs[i].getX0());
					input1[i] = Encoding.encodeBoolean(inputs[i].getX1());
				}
				
				if (seperateProcess) {
					String base64input0 = Base64.getEncoder().encodeToString(input0);
					String base64input1 = Base64.getEncoder().encodeToString(input1);

					ProcessBuilder builder = new ProcessBuilder(OTConfig.SCAPI_CMD, "-cp",
							OTConfig.CLASSPATH, OTConfig.OT_SENDER, host, Integer.toString(port),
							base64input0, base64input1);
					
					String path = new File("") + OTConfig.PATH;	
					
					builder.directory(new File(path));
					Process p = builder.start();
					p.waitFor();
				} else {
					// TODO: Not usable in JUnit tests so this has NOT been tested yet.
					throw new NotImplementedException();
					//NativeOTSender.send(host, port, input0, input1);
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return;
	}

}
