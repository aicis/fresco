package dk.alexandra.fresco.suite.tinytables.util.ot;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTInput;

public class OTSender {


	public static void transfer(String host, int port, OTInput[] inputs) {

		try {
			
			byte[] input0 = new byte[inputs.length];
			byte[] input1 = new byte[inputs.length];
			
			for (int i = 0; i < inputs.length; i++) {
				input0[i] = Encoding.encodeBoolean(inputs[i].getX0());
				input1[i] = Encoding.encodeBoolean(inputs[i].getX1());
			}

			if (input0.length > OTConfig.MAX_OTS) {
				transfer(host, port, Arrays.copyOfRange(inputs, 0, OTConfig.MAX_OTS));
				transfer(host, port, Arrays.copyOfRange(inputs, OTConfig.MAX_OTS, inputs.length));
			} else {
				String base64input0 = Base64.getEncoder().encodeToString(input0);
				String base64input1 = Base64.getEncoder().encodeToString(input1);

				ProcessBuilder builder = new ProcessBuilder(OTConfig.SCAPI_CMD, OTConfig.OT_SENDER,
						host, Integer.toString(port), base64input0, base64input1);
				
				String path = new File("") + OTConfig.PATH;	
				
				builder.directory(new File(path));
				Process p = builder.start();
				p.waitFor();
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
