package dk.alexandra.fresco.integrationtest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.gson.Gson;

import dk.alexandra.fresco.rest.FuelEndpoint;
import dk.alexandra.fresco.services.DataGeneratorImpl;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.utils.Util;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { 
		FuelEndpoint.class,
		DataGeneratorImpl.class
})
@WebAppConfiguration
public class FuelStationTest {

	@Autowired
	private FuelEndpoint fuelEndpoint;

	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.standaloneSetup(fuelEndpoint).build();		
	}

	private SpdzTriple[] convertToTriples(InputStream is, int amount) throws IOException {
		SpdzTriple[] res = new SpdzTriple[amount];
		for(int i = 0; i < amount; i++) {
			int elmSize = Util.getModulusSize()*2;
			byte[] a = new byte[elmSize];
			byte[] b = new byte[elmSize];
			byte[] c = new byte[elmSize];
			is.read(a);
			is.read(b);
			is.read(c);
			res[i] = new SpdzTriple(new SpdzElement(a), new SpdzElement(b), new SpdzElement(c));
		}
		return res;
	}
	
	@Test
	public void integrationTestTriples() throws Exception {
		
		MockHttpServletResponse resp = this.mockMvc
				.perform(
						get("/api/fuel/modulus").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();
		String modString = resp.getContentAsString();
		BigInteger mod = new BigInteger(modString);
		
		int amount = 5000;
		
		resp = this.mockMvc
				.perform(
						get("/api/fuel/triples/"+amount+"/party/1/thread/0").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();
		
		SpdzTriple[] triple11 = convertToTriples(new ByteArrayInputStream(resp.getContentAsByteArray()), amount);
		
		resp = this.mockMvc
				.perform(
						get("/api/fuel/triples/"+amount+"/party/1/thread/0").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		SpdzTriple[] triple21 = convertToTriples(new ByteArrayInputStream(resp.getContentAsByteArray()), amount);
		Assert.assertNotEquals(triple11, triple21);
		
		resp = this.mockMvc
				.perform(
						get("/api/fuel/triples/"+amount+"/party/2/thread/0").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		SpdzTriple[] triple12 = convertToTriples(new ByteArrayInputStream(resp.getContentAsByteArray()), amount);
		for(int i = 0; i < amount; i++) {
			BigInteger a = triple11[i].getA().getShare().add(triple12[i].getA().getShare()).mod(mod);
			BigInteger b = triple11[i].getB().getShare().add(triple12[i].getB().getShare()).mod(mod);
			BigInteger c = triple11[i].getC().getShare().add(triple12[i].getC().getShare()).mod(mod);
			
			Assert.assertEquals(a.multiply(b).mod(mod), c);
		}
	}
	
	@Test
	public void integrationTestBits() throws Exception {
		
		String modString = this.mockMvc
				.perform(
						get("/api/fuel/modulus").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		BigInteger mod = new BigInteger(modString);
		
		MockHttpServletResponse content = this.mockMvc
				.perform(
						get("/api/fuel/bits/1/party/1/thread/0").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();		
		
		SpdzElement bit1 = new SpdzElement(content.getContentAsByteArray());
		
		content = this.mockMvc
				.perform(
						get("/api/fuel/bits/1/party/2/thread/0").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		SpdzElement bit2 = new SpdzElement(content.getContentAsByteArray());
		
		BigInteger bit = bit1.getShare().add(bit2.getShare()).mod(mod);
		Assert.assertTrue(bit.equals(BigInteger.ZERO) || bit.equals(BigInteger.ONE));
	}
	
	@Test
	public void integrationTestExpPipes() throws Exception {
		String modString = this.mockMvc
				.perform(
						get("/api/fuel/modulus").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		BigInteger mod = new BigInteger(modString);
		
		MockHttpServletResponse content = this.mockMvc
				.perform(
						get("/api/fuel/exp/1/party/1/thread/0").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();
				
		byte[] bs = content.getContentAsByteArray();
		ByteArrayInputStream bis = new ByteArrayInputStream(bs);
		SpdzElement[] exp1 = new SpdzElement[Util.EXP_PIPE_SIZE];
		for(int i = 0; i < exp1.length; i++) {
			byte[] elm = new byte[Util.getModulusSize()*2];
			bis.read(elm);
			exp1[i] = new SpdzElement(elm);
		}
		
		content = this.mockMvc
				.perform(
						get("/api/fuel/exp/1/party/2/thread/0").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		bs = content.getContentAsByteArray();
		bis = new ByteArrayInputStream(bs);
		SpdzElement[] exp2 = new SpdzElement[Util.EXP_PIPE_SIZE];
		for(int i = 0; i < exp2.length; i++) {
			byte[] elm = new byte[Util.getModulusSize()*2];
			bis.read(elm);
			exp2[i] = new SpdzElement(elm);
		}

		BigInteger rInv = exp1[0].getShare().add(exp2[0].getShare()).mod(mod);
		BigInteger r = exp1[1].getShare().add(exp2[1].getShare()).mod(mod);
		BigInteger rSquared = exp1[2].getShare().add(exp2[2].getShare()).mod(mod);
		Assert.assertEquals(r.modInverse(mod), rInv);
		Assert.assertEquals(r.multiply(r).mod(mod), rSquared);
	}
	
	private SpdzInputMask convertToMask(byte[] arr) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(arr);
		int length = is.read();
		byte[] elm = new byte[Util.getModulusSize()*2];
		if(length == 0) {
			is.read(elm);
			return new SpdzInputMask(new SpdzElement(elm));
		} else {
			byte[] real = new byte[length];
			is.read(real);
			is.read(elm);
			return new SpdzInputMask(new SpdzElement(elm), new BigInteger(real));
		}
	}
	
	@Test
	public void integrationTestInputMasks() throws Exception {
		
		String modString = this.mockMvc
				.perform(
						get("/api/fuel/modulus").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		BigInteger mod = new BigInteger(modString);
		
		MockHttpServletResponse content = this.mockMvc
				.perform(
						get("/api/fuel/inputs/1/party/1/towards/1/thread/0").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();
		
		SpdzInputMask mask1 = convertToMask(content.getContentAsByteArray());		
		content = this.mockMvc
				.perform(
						get("/api/fuel/inputs/1/party/2/towards/1/thread/0").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		SpdzInputMask mask2 = convertToMask(content.getContentAsByteArray());
		Assert.assertEquals(mask1.getRealValue(), mask1.getMask().getShare().add(mask2.getMask().getShare()).mod(mod));
		
		content = this.mockMvc
				.perform(
						get("/api/fuel/inputs/1/party/1/towards/2/thread/0").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();
		
		mask1 = convertToMask(content.getContentAsByteArray());
		
		content = this.mockMvc
				.perform(
						get("/api/fuel/inputs/1/party/2/towards/2/thread/0").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		mask2 = convertToMask(content.getContentAsByteArray());
		
		Assert.assertEquals(mask2.getRealValue(), mask1.getMask().getShare().add(mask2.getMask().getShare()).mod(mod));
	}

}
