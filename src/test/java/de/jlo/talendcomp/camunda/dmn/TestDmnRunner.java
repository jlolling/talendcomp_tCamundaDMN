package de.jlo.talendcomp.camunda.dmn;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestDmnRunner {
	
	@Test
	public void testOneResult() throws Exception {
		String season = "Winter";
		int guestCount = 2;
		// this runs within the BEGIN part
		DmnRunner runner = new DmnRunner();
		runner.loadDmnFromResource("decision", "/dish-decision.dmn11.dmn");
		runner.addExpectedInputVariable("season");
		runner.addExpectedInputVariable("guestCount");
		runner.validateInputVariables();
		runner.addExpectedOutputVariable("desiredDish");
		runner.validateOutputVariables();
		// this runs within the MAIN part
		runner.clearVariables();
		runner.addInputValue("season", season);
		runner.addInputValue("guestCount", guestCount);
		runner.evaluate();
		if (runner.next()) {
			String dish = (String) runner.getOutputValue("desiredDish");
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
		runner.addExpectedInputVariable("product");
		runner.addExpectedInputVariable("type");
		runner.addExpectedInputVariable("grade");
		runner.addExpectedInputVariable("width");
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
			Integer surcharge = (Integer) runner.getOutputValue("surcharge");
			System.out.println(surcharge);
			if (count == 1) {
				assertEquals((long) 10, (long) surcharge);
			} else {
				assertEquals((long) 250, (long) surcharge);
			}
		}
		assertEquals(2, count);
	}

}