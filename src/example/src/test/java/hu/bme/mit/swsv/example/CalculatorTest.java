package hu.bme.mit.swsv.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class CalculatorTest {

	@Test
	public void testAdd() {
		// Arrange
		Calculator sut = new Calculator();

		// Act
		int result = sut.add(1, 2);

		// Assert
		assertEquals(3, result);
	}

}