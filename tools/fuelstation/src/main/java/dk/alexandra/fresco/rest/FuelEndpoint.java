package dk.alexandra.fresco.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import dk.alexandra.fresco.services.DataGenerator;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;

@RestController
@RequestMapping("/api/fuel")
public class FuelEndpoint {
	
	@Autowired
	private DataGenerator generator;
	
	@RequestMapping(value="/triples/{amount}/party/{partyId}/thread/{thread}", method = RequestMethod.GET)
	private void generateTriples(@PathVariable int amount, @PathVariable int partyId, @PathVariable int thread, HttpServletResponse response) throws InterruptedException, IOException {
		SpdzTriple[] triples = generator.getTriples(amount, partyId, thread);
//		int elmLength = Util.getModulusSize()*2;
//		int contentLength = elmLength*3*amount;
//		response.setContentLength(contentLength);
		OutputStream os = response.getOutputStream();
		for(SpdzTriple t : triples) {
			os.write(t.getA().toByteArray());
			os.write(t.getB().toByteArray());
			os.write(t.getC().toByteArray());
			os.flush();
		}	
	}
	
	@RequestMapping(value="/bits/{amount}/party/{partyId}/thread/{thread}", method = RequestMethod.GET)
	private void generateBits(@PathVariable int amount, @PathVariable int partyId, @PathVariable int thread, HttpServletResponse response) throws InterruptedException, IOException {
		SpdzElement[] bits = generator.getBits(amount, partyId, thread);
		OutputStream os = response.getOutputStream();
		for(SpdzElement b : bits) {
			os.write(b.toByteArray());
			os.flush();
		}
	}
	
	@RequestMapping(value="/exp/{amount}/party/{partyId}/thread/{thread}", method = RequestMethod.GET)
	private void generateExpPipes(@PathVariable int amount, @PathVariable int partyId, @PathVariable int thread, HttpServletResponse response) throws InterruptedException, IOException {
		SpdzElement[][] pipes = generator.getExpPipes(amount, partyId, thread);
		OutputStream os = response.getOutputStream();
		for(SpdzElement[] p : pipes) {			
			for(SpdzElement elm : p) {
				os.write(elm.toByteArray());
			}
			os.flush();
		}
	}
	
	@RequestMapping(value="/inputs/{amount}/party/{partyId}/towards/{towardsId}/thread/{thread}", method = RequestMethod.GET)
	private void generateInputMasks(@PathVariable int amount, @PathVariable int partyId, @PathVariable int towardsId, @PathVariable int thread, HttpServletResponse response) throws InterruptedException, IOException {		
		SpdzInputMask[] masks = generator.getInputMasks(amount, partyId, towardsId, thread);
		OutputStream os = response.getOutputStream();
		for(SpdzInputMask m : masks) {
			if(m.getRealValue() == null) {
				os.write(0);	
			} else {
				byte[] real = m.getRealValue().toByteArray();
				os.write(real.length);
				os.write(real);
			}
			os.write(m.getMask().toByteArray());
			os.flush();
		}
	}
	
	@RequestMapping(value="/modulus", method = RequestMethod.GET)
	private BigInteger getModulus() {
		return generator.getModulus();
	}
	
	@RequestMapping(value="/alpha/{partyId}", method = RequestMethod.GET)
	private BigInteger getAlpha(@PathVariable int partyId) throws IllegalArgumentException {
		return generator.getAlpha(partyId);		
	}
	
	@RequestMapping(value="/reset/{partyId}", method = RequestMethod.GET)
	private Boolean reset(@PathVariable int partyId) throws IllegalArgumentException {
		return generator.reset(partyId);		
	}
}
