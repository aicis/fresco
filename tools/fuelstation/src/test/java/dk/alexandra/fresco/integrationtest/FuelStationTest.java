package dk.alexandra.fresco.integrationtest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
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
		
		resp = this.mockMvc
				.perform(
						get("/api/fuel/triples/1/1").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();
		
		Gson gson = new Gson();
		SpdzTriple[] ts = gson.fromJson(resp.getContentAsString(), SpdzTriple[].class);
		
		SpdzTriple triple11 = ts[0];
		
		String content = this.mockMvc
				.perform(
						get("/api/fuel/triples/1/1").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		ts = gson.fromJson(content, SpdzTriple[].class);
		SpdzTriple triple21 = ts[0];
		Assert.assertNotEquals(triple11, triple21);
		
		content = this.mockMvc
				.perform(
						get("/api/fuel/triples/1/2").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		ts = gson.fromJson(content, SpdzTriple[].class);
		SpdzTriple triple12 = ts[0];
		BigInteger a = triple11.getA().getShare().add(triple12.getA().getShare()).mod(mod);
		BigInteger b = triple11.getB().getShare().add(triple12.getB().getShare()).mod(mod);
		BigInteger c = triple11.getC().getShare().add(triple12.getC().getShare()).mod(mod);
		
		Assert.assertEquals(a.multiply(b).mod(mod), c);
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
		
		String content = this.mockMvc
				.perform(
						get("/api/fuel/bits/1/1").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		
		Gson gson = new Gson();
		
		SpdzElement bit1 = gson.fromJson(content, SpdzElement[].class)[0];
		
		content = this.mockMvc
				.perform(
						get("/api/fuel/bits/1/2").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		SpdzElement bit2 = gson.fromJson(content, SpdzElement[].class)[0];
		
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
		
		String content = this.mockMvc
				.perform(
						get("/api/fuel/exp/1/1").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		
		Gson gson = new Gson();
		SpdzElement[] exp1 = gson.fromJson(content, SpdzElement[][].class)[0];
		
		content = this.mockMvc
				.perform(
						get("/api/fuel/exp/1/2").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		SpdzElement[] exp2 = gson.fromJson(content, SpdzElement[][].class)[0];

		BigInteger rInv = exp1[0].getShare().add(exp2[0].getShare()).mod(mod);
		BigInteger r = exp1[1].getShare().add(exp2[1].getShare()).mod(mod);
		BigInteger rSquared = exp1[2].getShare().add(exp2[2].getShare()).mod(mod);
		Assert.assertEquals(r.modInverse(mod), rInv);
		Assert.assertEquals(r.multiply(r).mod(mod), rSquared);
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
		
		String content = this.mockMvc
				.perform(
						get("/api/fuel/inputs/1/1/towards/1").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		
		Gson gson = new Gson();		
		SpdzInputMask mask1 = gson.fromJson(content, SpdzInputMask[].class)[0];		
		System.out.println(mask1);
		content = this.mockMvc
				.perform(
						get("/api/fuel/inputs/1/2/towards/1").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		SpdzInputMask mask2 = gson.fromJson(content, SpdzInputMask[].class)[0];
		System.out.println(mask2);		
		Assert.assertEquals(mask1.getRealValue(), mask1.getMask().getShare().add(mask2.getMask().getShare()).mod(mod));
		
		content = this.mockMvc
				.perform(
						get("/api/fuel/inputs/1/1/towards/2").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		
		mask1 = gson.fromJson(content, SpdzInputMask[].class)[0];
		
		content = this.mockMvc
				.perform(
						get("/api/fuel/inputs/1/2/towards/2").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		mask2 = gson.fromJson(content, SpdzInputMask[].class)[0];
		
		Assert.assertEquals(mask2.getRealValue(), mask1.getMask().getShare().add(mask2.getMask().getShare()).mod(mod));
	}

}
