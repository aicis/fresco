package dk.alexandra.fresco.rest;

import java.io.IOException;
import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import dk.alexandra.fresco.services.Preprocessor;

@RestController
@RequestMapping("/api/fuel")
@PropertySource("classpath:prepro.properties")
public class FuelEndpoint {

	@Value("${mod}")
	private BigInteger mod;
	
	@Value("${alpha1}")
	private BigInteger alpha1;
	
	@Value("${alpha2}")
	private BigInteger alpha2;
	
	@Autowired
	private Preprocessor preprocesser;
	
	@RequestMapping(value="/triples/{amount}/{partyId}", method = RequestMethod.GET)
	private byte[] generateTriples(@PathVariable int amount, @PathVariable int partyId) throws IOException {		
		byte[] res = preprocesser.getTriples(amount, partyId);
		return res;
	}
	
	@RequestMapping(value="/bits/{amount}/{partyId}", method = RequestMethod.GET)
	private byte[] generateBits(@PathVariable int amount, @PathVariable int partyId) throws IOException {
		return preprocesser.getBits(amount, partyId);
	}
	
	@RequestMapping(value="/exp/{amount}/{partyId}", method = RequestMethod.GET)
	private byte[] generateExpPipes(@PathVariable int amount, @PathVariable int partyId) throws IOException {
		return preprocesser.getExpPipes(amount, partyId);
	}
	
	@RequestMapping(value="/inputs/{amount}/{partyId}/towards/{towardsId}", method = RequestMethod.GET)
	private byte[] generateInputMasks(@PathVariable int amount, @PathVariable int partyId, @PathVariable int towardsId) throws IOException {
		return preprocesser.getInputMasks(amount, partyId, towardsId);
	}
	
	@RequestMapping(value="/modulus", method = RequestMethod.GET)
	private BigInteger getModulus() {
		return mod;
	}
	
	@RequestMapping(value="/alpha/{partyId}", method = RequestMethod.GET)
	private BigInteger getAlpha(@PathVariable int partyId) throws IllegalArgumentException {
		if(partyId == 1) {
			return alpha1;
		} else if(partyId == 2) {
			return alpha2;
		} else {
			throw new IllegalArgumentException("Given partyId does not exist. PartyId was: " + partyId);
		}
	}
}
