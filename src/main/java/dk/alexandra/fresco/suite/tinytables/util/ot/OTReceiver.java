package dk.alexandra.fresco.suite.tinytables.util.ot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

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
public class OTReceiver {

	public static byte[] transfer(String host, int port, byte[] sigmas) {
		
		try {
			
			/*
			 * There is an upper bound on how big a terminal cmd can be, so if
			 * we have too many OT's we need to do them a batch at a time.
			 */
			if (sigmas.length > OTConfig.MAX_OTS) {
				byte[] out1 = transfer(host, port, Arrays.copyOfRange(sigmas, 0, OTConfig.MAX_OTS));
				byte[] out2 = transfer(host, port, Arrays.copyOfRange(sigmas, OTConfig.MAX_OTS, sigmas.length));
				
				byte[] out = new byte[out1.length + out2.length];
				System.arraycopy(out1, 0, out, 0, out1.length);
				System.arraycopy(out2, 0, out, out1.length, out2.length);
				return out;
			} else {
				String base64sigmas = Base64.getEncoder().encodeToString(sigmas);
				
				ProcessBuilder builder = new ProcessBuilder(OTConfig.SCAPI_CMD, OTConfig.OT_RECEIVER, host,
						Integer.toString(port), base64sigmas);
				builder.directory(new File(OTConfig.PATH));
				Process p = builder.start();
				
				p.waitFor();
				
				String base64output = new BufferedReader(new InputStreamReader(p.getInputStream()))
				.lines().collect(Collectors.joining("\n"));
				
				byte[] output = Base64.getDecoder().decode(base64output);
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
