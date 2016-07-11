package dk.alexandra.fresco.suite.tinytables.util.ot;


public class OTConfig {
	
	public static final String PATH = "target/classes";
	
	public static final String OT_RECEIVER = "dk/alexandra/fresco/suite/tinytables/util/ot/scapi/NativeOTReceiver";
	
	public static final String OT_SENDER = "dk/alexandra/fresco/suite/tinytables/util/ot/scapi/NativeOTSender";
	
	/*
	 * Note that SCAPI needs to be installed (see
	 * http://scapi.readthedocs.io/en/latest/install.html).
	 * 
	 * TODO: Actually the scapi command is just a script which adds Apache
	 * Commons, Scapi and BouncyCastle to the classpath, so we should be able to
	 * do this with a 'java' command instead.
	 */
	public static final String SCAPI_CMD = "/usr/local/bin/scapi";
	
	public static final int MAX_OTS = 10000;

}
