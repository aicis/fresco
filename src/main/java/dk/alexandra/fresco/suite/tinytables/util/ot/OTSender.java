package dk.alexandra.fresco.suite.tinytables.util.ot;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

public class OTSender {


	public static void transfer(String host, int port, byte[] input0, byte[] input1) {

		try {

			if (input0.length > OTConfig.MAX_OTS) {
				transfer(host, port, Arrays.copyOfRange(input0, 0, OTConfig.MAX_OTS),
						Arrays.copyOfRange(input1, 0, OTConfig.MAX_OTS));
				transfer(host, port, Arrays.copyOfRange(input0, OTConfig.MAX_OTS, input0.length),
						Arrays.copyOfRange(input1, OTConfig.MAX_OTS, input1.length));
			} else {
				String base64input0 = Base64.getEncoder().encodeToString(input0);
				String base64input1 = Base64.getEncoder().encodeToString(input1);

				ProcessBuilder builder = new ProcessBuilder(OTConfig.SCAPI_CMD, OTConfig.OT_SENDER,
						host, Integer.toString(port), base64input0, base64input1);
				
				builder.directory(new File(OTConfig.PATH));
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
