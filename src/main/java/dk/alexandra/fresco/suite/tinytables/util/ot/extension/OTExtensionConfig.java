package dk.alexandra.fresco.suite.tinytables.util.ot.extension;

public class OTExtensionConfig {

	public static final String PATH = "target/classes";

	public static final String OT_RECEIVER = "dk/alexandra/fresco/suite/tinytables/util/ot/extension/OTExtensionReceiverApplication";

	public static final String OT_SENDER = "dk/alexandra/fresco/suite/tinytables/util/ot/extension/OTExtensionSenderApplication";

	public static final String SCAPI_CMD = "java";

	public static final String CLASSPATH = "../../lib/scapi/scapi/dfc8b2da384d87310c8f755b301bc085b1557671/scapi-dfc8b2da384d87310c8f755b301bc085b1557671.jar:.";

	public static final int MAX_OTS = 10000;

	/**
	 * This method checks whether the OtExtensionJavaInterface library from
	 * SCAPI is available on this platform. Check
	 * http://scapi.readthedocs.io/en/latest/install.html for help on how to
	 * install SCAPI on specific platforms.
	 * 
	 * @return
	 */
	public static boolean hasOTExtensionLib() {
		try {
			String javaLibPath = System.getProperty("java.library.path");
			System.out.println(javaLibPath);

			System.loadLibrary("OtExtensionJavaInterface");
			return true;
		} catch (java.lang.UnsatisfiedLinkError e) {
			return false;
		}
	}

}
