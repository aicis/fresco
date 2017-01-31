package dk.alexandra.fresco.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import dk.alexandra.fresco.model.Type;
import dk.alexandra.fresco.suite.spdz.storage.FakeTripGen;

@Component
@PropertySource("classpath:prepro.properties")
public class GeneratePreprocessedData {

	Logger logger = LoggerFactory.getLogger(GeneratePreprocessedData.class);
	
	@Value("${mod}")
	private BigInteger mod;
	
	@Value("${alpha1}")
	private BigInteger alpha1;
	
	@Value("${alpha2}")
	private BigInteger alpha2;
	
	public static final String FUEL_DIR = "fuel";
	public static final int noOfPlayers = 2;
	private int tripleFileCounter = 0;
	private int bitFileCounter = 0;
	private int expFileCounter = 0;
	private int input1FileCounter = 0;
	private int input2FileCounter = 0;
	
	@PostConstruct
	public void clearAndInit() throws IOException {
		File directory = new File(FUEL_DIR);
		try {
			FileUtils.deleteDirectory(directory);
		} catch (IOException e) {
			logger.warn("Could not delete the directory "+directory.getAbsolutePath()+". This is likely because it did not exist.");
		}
		Files.createDirectory(Paths.get("fuel"));
	}
	
	@SuppressWarnings("serial")
	public void generate(int amount, Type type) throws IOException{
		BigInteger alpha = alpha1.add(alpha2).mod(mod);
		List<ObjectOutputStream> streams = new ArrayList<>();
		switch(type) {
		case TRIPLES:			
			for(int i = 1; i <= noOfPlayers; i++) {
				File file = new File(FUEL_DIR+"/Triple_"+i+"_"+tripleFileCounter);
				FileOutputStream fos = new FileOutputStream(file);
				streams.add(new ObjectOutputStream(fos));
			}
			tripleFileCounter++;
			new FakeTripGen().generateTripleStream(amount, noOfPlayers, mod, alpha, new SecureRandom(), new ArrayList<List<ObjectOutputStream>>() {{add(streams);}});
			break;
		case BITS:			
			for(int i = 1; i <= noOfPlayers; i++) {
				File file = new File(FUEL_DIR+"/Bits_"+i+"_"+bitFileCounter);
				FileOutputStream fos = new FileOutputStream(file);
				streams.add(new ObjectOutputStream(fos));
			}
			bitFileCounter++;
			new FakeTripGen().generateBitStream(amount, noOfPlayers, mod, alpha, new SecureRandom(), new ArrayList<List<ObjectOutputStream>>() {{add(streams);}});			
			break;
		case EXPPIPES:
			for(int i = 1; i <= noOfPlayers; i++) {
				File file = new File(FUEL_DIR+"/Exp_"+i+"_"+expFileCounter);
				FileOutputStream fos = new FileOutputStream(file);
				streams.add(new ObjectOutputStream(fos));
			}
			expFileCounter++;
			new FakeTripGen().generateExpPipeStream(amount, noOfPlayers, mod, alpha, new SecureRandom(), new ArrayList<List<ObjectOutputStream>>() {{add(streams);}});
			break;
		case INPUT_1:
			for(int i = 1; i <= noOfPlayers; i++) {
				File file = new File(FUEL_DIR+"/Input_"+i+"_"+input1FileCounter+"_1");
				FileOutputStream fos = new FileOutputStream(file);
				streams.add(new ObjectOutputStream(fos));
			}
			input1FileCounter++;
			new FakeTripGen().generateInputMaskStream(amount, noOfPlayers, 0, mod, alpha, new SecureRandom(), new ArrayList<List<ObjectOutputStream>>() {{add(streams);}});
			break;
		case INPUT_2:
			for(int i = 1; i <= noOfPlayers; i++) {
				File file = new File(FUEL_DIR+"/Input_"+i+"_"+input2FileCounter+"_2");
				FileOutputStream fos = new FileOutputStream(file);
				streams.add(new ObjectOutputStream(fos));
			}
			input2FileCounter++;
			new FakeTripGen().generateInputMaskStream(amount, noOfPlayers, 1, mod, alpha, new SecureRandom(), new ArrayList<List<ObjectOutputStream>>() {{add(streams);}});
			break;
		}		
				
		for(ObjectOutputStream oos : streams) {
			oos.flush();
			oos.close();
		}
	}
}
