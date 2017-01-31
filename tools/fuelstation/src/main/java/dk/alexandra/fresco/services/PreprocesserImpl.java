package dk.alexandra.fresco.services;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import dk.alexandra.fresco.model.Type;

@Component
@PropertySource("classpath:prepro.properties")
public class PreprocesserImpl implements Preprocessor {

	Logger logger = LoggerFactory.getLogger(PreprocesserImpl.class);

	@Value("${mod}")
	private BigInteger mod;

	@Value("${alpha1}")
	private BigInteger alpha1;

	@Value("${alpha2}")
	private BigInteger alpha2;

	@Autowired
	private GeneratePreprocessedData generator;

	private Map<Integer, Integer> tripleFileCounters = new HashMap<>();
	private Map<Integer, Integer> bitFileCounters = new HashMap<>();
	private Map<Integer, Integer> expPipeFileCounters = new HashMap<>();		
	private Map<Integer, Integer> input1FileCounters = new HashMap<>();
	private Map<Integer, Integer> input2FileCounters = new HashMap<>();

	@PostConstruct
	private void init() {
		tripleFileCounters.put(1, 0);
		tripleFileCounters.put(2, 0);
		bitFileCounters.put(1, 0);
		bitFileCounters.put(2, 0);
		expPipeFileCounters.put(1, 0);
		expPipeFileCounters.put(2, 0);
		input1FileCounters.put(1, 0);
		input1FileCounters.put(2, 0);
		input2FileCounters.put(1, 0);
		input2FileCounters.put(2, 0);
	}

	private byte[] generateAndReadOrFail(Path path, int amount, Type type) throws IOException{
		byte[] res = null;
		try{
			res = Files.readAllBytes(path);
		} catch(NoSuchFileException e) {
			this.generator.generate(amount, type);
			try {
				res = Files.readAllBytes(path);
			} catch(NoSuchFileException ex) {
				logger.error("Could not find the generated "+type+" file even though we just tried to generate it. Should be here: " + path);
				ex.printStackTrace();
				throw ex;
			}
		}		
		return res;
	}

	@Override
	public synchronized byte[] getTriples(int amount, int partyId) throws IOException {
		int tripleFileCounter = tripleFileCounters.get(partyId);
		Path path = Paths.get(GeneratePreprocessedData.FUEL_DIR, "Triple_"+partyId+"_"+tripleFileCounter);
		byte[] res = generateAndReadOrFail(path, amount, Type.TRIPLES);
		tripleFileCounters.put(partyId, tripleFileCounter+1);
		return res;
	}

	@Override
	public synchronized byte[] getBits(int amount, int partyId) throws IOException {
		int bitFileCounter = bitFileCounters.get(partyId);
		Path path = Paths.get(GeneratePreprocessedData.FUEL_DIR, "Bits_"+partyId+"_"+bitFileCounter);		
		byte[] res = generateAndReadOrFail(path, amount, Type.BITS);		
		bitFileCounters.put(partyId, bitFileCounter+1);		
		return res;
	}

	@Override
	public synchronized byte[] getExpPipes(int amount, int partyId) throws IOException {
		int expFileCounter = expPipeFileCounters.get(partyId);
		Path path = Paths.get(GeneratePreprocessedData.FUEL_DIR, "Exp_"+partyId+"_"+expFileCounter);
		byte[] res = generateAndReadOrFail(path, amount, Type.EXPPIPES);		
		expPipeFileCounters.put(partyId, expFileCounter+1);
		return res;
	}

	@Override
	public BigInteger getModulus() {
		return mod;
	}

	@Override
	public BigInteger alpha(int partyId) {
		if(partyId == 1) {
			return alpha1;
		} else if(partyId == 2) {
			return alpha2;
		} else {
			throw new IllegalArgumentException("The given partyId has an invalid value: "+partyId);
		}
	}

	@Override
	public synchronized byte[] getInputMasks(int amount, int partyId, int towardsPartyId) throws IOException {
		if(towardsPartyId == 1) {
			int inputFileCounter = input1FileCounters.get(partyId);
			Path path = Paths.get(GeneratePreprocessedData.FUEL_DIR, "Input_"+partyId+"_"+inputFileCounter+"_1");
			byte[] res = generateAndReadOrFail(path, amount, Type.INPUT_1);		
			input1FileCounters.put(partyId, inputFileCounter+1);
			return res;
		} else if(towardsPartyId == 2) {
			int inputFileCounter = input2FileCounters.get(partyId);
			Path path = Paths.get(GeneratePreprocessedData.FUEL_DIR, "Input_"+partyId+"_"+inputFileCounter+"_2");
			byte[] res = generateAndReadOrFail(path, amount, Type.INPUT_2);		
			input2FileCounters.put(partyId, inputFileCounter+1);
			return res;
		} else {
			throw new IllegalArgumentException("The given partyId has an invalid value: " + partyId);
		}
	}

}
