package dk.alexandra.fresco.suite.tinytables.util.ot.extension;


public class OTExtensionConfig {
	
	public static final String PATH = "target/classes";
	
	public static final String OT_RECEIVER = "dk/alexandra/fresco/suite/tinytables/util/ot/extension/OTExtensionReceiverApplication";
	
	public static final String OT_SENDER = "dk/alexandra/fresco/suite/tinytables/util/ot/extension/OTExtensionSenderApplication";
	
	public static final String SCAPI_CMD = "java";

	public static final String CLASSPATH = "../../lib/scapi/scapi/dfc8b2da384d87310c8f755b301bc085b1557671/scapi-dfc8b2da384d87310c8f755b301bc085b1557671.jar:.";
	
	public static final int MAX_OTS = 10000;

	public static boolean hasOTExtensionLib() {
		try {
			System.loadLibrary("OtExtensionJavaInterface");	
			return true;
		} catch (java.lang.UnsatisfiedLinkError e) {
			return false;
		}
	}
	
}
