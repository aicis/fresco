package dk.alexandra.fresco.rest;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import dk.alexandra.fresco.services.DataGenerator;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;

@RestController
@RequestMapping("/api/fuel")
public class FuelEndpoint {
	
	@Autowired
	private DataGenerator generator;
	
	@RequestMapping(value="/triples/{amount}/{partyId}", method = RequestMethod.GET)
	private SpdzTriple[] generateTriples(@PathVariable int amount, @PathVariable int partyId) throws InterruptedException {		
		return generator.getTriples(amount, partyId);
	}
	
	@RequestMapping(value="/bits/{amount}/{partyId}", method = RequestMethod.GET)
	private SpdzElement[] generateBits(@PathVariable int amount, @PathVariable int partyId) throws InterruptedException {
		return generator.getBits(amount, partyId);
	}
	
	@RequestMapping(value="/exp/{amount}/{partyId}", method = RequestMethod.GET)
	private SpdzElement[][] generateExpPipes(@PathVariable int amount, @PathVariable int partyId) throws InterruptedException {
		return generator.getExpPipes(amount, partyId);
	}
	
	@RequestMapping(value="/inputs/{amount}/{partyId}/towards/{towardsId}", method = RequestMethod.GET)
	private SpdzInputMask[] generateInputMasks(@PathVariable int amount, @PathVariable int partyId, @PathVariable int towardsId) throws InterruptedException {
		return generator.getInputMasks(amount, partyId, towardsId);
	}
	
	@RequestMapping(value="/modulus", method = RequestMethod.GET)
	private BigInteger getModulus() {
		return generator.getModulus();
	}
	
	@RequestMapping(value="/alpha/{partyId}", method = RequestMethod.GET)
	private BigInteger getAlpha(@PathVariable int partyId) throws IllegalArgumentException {
		return generator.getAlpha(partyId);		
	}
}
