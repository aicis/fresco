package dk.alexandra.fresco.integrationtest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;

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

import dk.alexandra.fresco.rest.FuelEndpoint;
import dk.alexandra.fresco.services.GeneratePreprocessedData;
import dk.alexandra.fresco.services.PreprocesserImpl;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { 
		FuelEndpoint.class,
		PreprocesserImpl.class,
		GeneratePreprocessedData.class
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

		byte[] content = resp.getContentAsByteArray();		
		
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(content));
		SpdzTriple triple11 = (SpdzTriple) ois.readObject();
		ois.close();
		
		content = this.mockMvc
				.perform(
						get("/api/fuel/triples/1/1").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsByteArray();

		ois = new ObjectInputStream(new ByteArrayInputStream(content));
		SpdzTriple triple21 = (SpdzTriple) ois.readObject();
		Assert.assertNotEquals(triple11, triple21);
		
		content = this.mockMvc
				.perform(
						get("/api/fuel/triples/1/2").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsByteArray();

		ois = new ObjectInputStream(new ByteArrayInputStream(content));
		SpdzTriple triple12 = (SpdzTriple) ois.readObject();
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
		
		byte[] content = this.mockMvc
				.perform(
						get("/api/fuel/bits/1/1").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsByteArray();
		
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(content));
		SpdzSInt bit1 = (SpdzSInt)ois.readObject();
		ois.close();
		
		content = this.mockMvc
				.perform(
						get("/api/fuel/bits/1/2").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsByteArray();

		ois = new ObjectInputStream(new ByteArrayInputStream(content));		
		
		SpdzSInt bit2 = (SpdzSInt)ois.readObject();
		ois.close();
		
		BigInteger bit = bit1.value.getShare().add(bit2.value.getShare()).mod(mod);
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
		
		byte[] content = this.mockMvc
				.perform(
						get("/api/fuel/exp/1/1").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsByteArray();
		
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(content));
		SpdzSInt[] exp1 = (SpdzSInt[])ois.readObject();
		ois.close();
		
		content = this.mockMvc
				.perform(
						get("/api/fuel/exp/1/2").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsByteArray();

		ois = new ObjectInputStream(new ByteArrayInputStream(content));		
		
		SpdzSInt[] exp2 = (SpdzSInt[])ois.readObject();
		ois.close();
		
		BigInteger rInv = exp1[0].value.getShare().add(exp2[0].value.getShare()).mod(mod);
		BigInteger r = exp1[1].value.getShare().add(exp2[1].value.getShare()).mod(mod);
		BigInteger rSquared = exp1[2].value.getShare().add(exp2[2].value.getShare()).mod(mod);
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
		
		byte[] content = this.mockMvc
				.perform(
						get("/api/fuel/inputs/1/1/towards/1").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsByteArray();
		
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(content));
		SpdzInputMask mask1 = (SpdzInputMask)ois.readObject();
		ois.close();
		
		content = this.mockMvc
				.perform(
						get("/api/fuel/inputs/1/2/towards/1").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsByteArray();

		ois = new ObjectInputStream(new ByteArrayInputStream(content));		
		
		SpdzInputMask mask2 = (SpdzInputMask)ois.readObject();
		ois.close();
				
		Assert.assertEquals(mask1.getRealValue(), mask1.getMask().getShare().add(mask2.getMask().getShare()).mod(mod));
		
		content = this.mockMvc
				.perform(
						get("/api/fuel/inputs/1/1/towards/2").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsByteArray();
		
		ois = new ObjectInputStream(new ByteArrayInputStream(content));
		mask1 = (SpdzInputMask)ois.readObject();
		ois.close();
		
		content = this.mockMvc
				.perform(
						get("/api/fuel/inputs/1/2/towards/2").contentType("application/json")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsByteArray();

		ois = new ObjectInputStream(new ByteArrayInputStream(content));		
		
		mask2 = (SpdzInputMask)ois.readObject();
		ois.close();
				
		Assert.assertEquals(mask2.getRealValue(), mask1.getMask().getShare().add(mask2.getMask().getShare()).mod(mod));
	}

}
