package de.jlo.talendcomp.camunda.dmn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.Test;

public class TestDmnRunner {
	
	@Test
	public void testOneResult() throws Exception {
		String season = "Winter";
		int guestCount = 2;
		// this runs within the BEGIN part
		DmnRunner runner = new DmnRunner();
		runner.loadDmnFromResource("decision", "/dish-decision.dmn11.dmn");
		runner.addAvailableInputVariable("season");
		runner.addAvailableInputVariable("guestCount");
		runner.validateInputVariables();
		runner.addExpectedOutputVariable("desiredDish");
		runner.validateOutputVariables();
		// this runs within the MAIN part
		runner.clearVariables();
		runner.addInputValue("season", season);
		runner.addInputValue("guestCount", guestCount);
		runner.evaluate();
		if (runner.next()) {
			String dish = (String) runner.getOutputValue("desiredDish", true);
			System.out.println(dish);
			assertEquals("Roastbeef", dish);
		}
	}

	@Test
	public void testMultipleRecords() throws Exception {
		String product = "Product1";
		String type = "1";
		String grade = "4307";
		int width = 2000;
		// this runs within the BEGIN part
		DmnRunner runner = new DmnRunner();
		runner.loadDmnFromResource("surcharge", "/collect_sum_example.dmn");
		runner.addAvailableInputVariable("product");
		runner.addAvailableInputVariable("type");
		runner.addAvailableInputVariable("grade");
		runner.addAvailableInputVariable("width");
		runner.validateInputVariables();
		runner.addExpectedOutputVariable("surcharge");
		runner.validateOutputVariables();
		// this runs within the MAIN part
		runner.clearVariables();
		runner.addInputValue("product", product);
		runner.addInputValue("type", type);
		runner.addInputValue("grade", grade);
		runner.addInputValue("width", width);
		runner.evaluate();
		System.out.println(runner.countResultRows());
		int count = 0;
		while (runner.next()) {
			count++;
			Integer surcharge = (Integer) runner.getOutputValue("surcharge", true);
			System.out.println(surcharge);
			if (count == 1) {
				assertEquals((long) 10, (long) surcharge);
			} else {
				assertEquals((long) 250, (long) surcharge);
			}
		}
		assertEquals(2, count);
	}

	@Test
	public void testNoRecords() throws Exception {
		String product = "Product1";
		String type = "1";
		String grade = "4307";
		int width = 0;
		// this runs within the BEGIN part
		DmnRunner runner = new DmnRunner();
		runner.loadDmnFromResource("surcharge", "/collect_sum_example.dmn");
		runner.setProvideOneRecordIfNoDecsionResult(true);
		runner.addAvailableInputVariable("product");
		runner.addAvailableInputVariable("type");
		runner.addAvailableInputVariable("grade");
		runner.addAvailableInputVariable("width");
		runner.validateInputVariables();
		runner.addExpectedOutputVariable("surcharge");
		runner.validateOutputVariables();
		// this runs within the MAIN part
		runner.clearVariables();
		runner.addInputValue("product", product);
		runner.addInputValue("type", type);
		runner.addInputValue("grade", grade);
		runner.addInputValue("width", width);
		runner.evaluate();
		System.out.println("Count tests: " + runner.countResultRows());
		int count = 0;
		while (runner.next()) {
			count++;
			Integer surcharge = (Integer) runner.getOutputValue("surcharge", true);
			System.out.println(surcharge);
		}
		assertEquals(1, count);
		System.out.println("##### add new row which should not return something");
		product = "Product0";
		type = "1";
		grade = "4307";
		width = 0;
		runner.clearVariables();
		runner.addInputValue("product", product);
		runner.addInputValue("type", type);
		runner.addInputValue("grade", grade);
		runner.addInputValue("width", width);
		runner.evaluate();
		System.out.println("Count tests: " + runner.countResultRows());
		count = 0;
		while (runner.next()) {
			count++;
			Integer surcharge = (Integer) runner.getOutputValue("surcharge", true);
			assertNull("Got value where no value should be returned", surcharge);
		}
		assertEquals(1, count);
	}
	
	@Test
	public void testGenerateCacheKey() throws Exception {
		String varStr = "äöü1234567890";
		Long varLong = 9999999l;
		Date varDate = new Date();
		DmnRunner runner = new DmnRunner();
		runner.addInputValue("varStr", varStr);
		runner.addInputValue("varLong", varLong);
		runner.addInputValue("varDate", varDate);
		Object key1 = runner.getValueKeyForCurrentVariables();
		System.out.println(key1);
		DmnRunner runner2 = new DmnRunner();
		runner2.addInputValue("varStr", varStr);
		runner2.addInputValue("varLong", varLong);
		runner2.addInputValue("varDate", varDate);
		Object key2 = runner2.getValueKeyForCurrentVariables();
		System.out.println(key2);
		assertEquals("Key generation is not equal", key1, key2);
	}

	@Test
	public void testCacheKeyNoCollision() throws Exception {
		DmnRunner runner = new DmnRunner();
		runner.addInputValue("a", "xb");
		runner.addInputValue("b", "1");
		DmnRunner runner2 = new DmnRunner();
		runner2.addInputValue("a", "x");
		runner2.addInputValue("b", "b1");
		assertNotEquals(runner.getValueKeyForCurrentVariables(), runner2.getValueKeyForCurrentVariables());
	}

	@Test
	public void testCachedDecisionIsValidated() throws Exception {
		DmnRunner runner = new DmnRunner();
		runner.setUseCachedDecision(true);
		runner.loadDmnFromResource("surcharge", "/collect_sum_example.dmn");
		DmnRunner runner2 = new DmnRunner();
		runner2.setUseCachedDecision(true);
		runner2.loadDmnFromResource("surcharge", "/collect_sum_example.dmn");
		runner2.addExpectedOutputVariable("not_existing_output");
		try {
			runner2.validateOutputVariables();
			fail("Validation of the cached decision must detect the not existing output");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testNoRecordsNotNullable() throws Exception {
		DmnRunner runner = new DmnRunner();
		runner.loadDmnFromResource("surcharge", "/collect_sum_example.dmn");
		runner.setProvideOneRecordIfNoDecsionResult(true);
		runner.clearVariables();
		runner.addInputValue("product", "Product0");
		runner.addInputValue("type", "1");
		runner.addInputValue("grade", "4307");
		runner.addInputValue("width", 0);
		runner.evaluate();
		assertTrue(runner.next());
		try {
			runner.getOutputValue("surcharge", false);
			fail("null for a not nullable column must fail");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
