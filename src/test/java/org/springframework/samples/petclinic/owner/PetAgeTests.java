/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PetAgeTests {

	private static final LocalDate TODAY = LocalDate.of(2026, 11, 20);

	@Test
	void petExactlyOneYearOldIsOneYear() {
		assertEquals(new PetAge("age.year", 1), PetAge.between(LocalDate.of(2025, 11, 20), TODAY));
	}

	@Test
	void petSeveralYearsOldCountsOnlyWholeYears() {
		assertEquals(new PetAge("age.years", 3), PetAge.between(LocalDate.of(2023, 5, 1), TODAY));
	}

	@Test
	void petOneDayShortOfAYearIsElevenMonths() {
		assertEquals(new PetAge("age.months", 11), PetAge.between(LocalDate.of(2025, 11, 21), TODAY));
	}

	@Test
	void petExactlyOneMonthOldIsOneMonth() {
		assertEquals(new PetAge("age.month", 1), PetAge.between(LocalDate.of(2026, 10, 20), TODAY));
	}

	@Test
	void petSeveralMonthsOldCountsOnlyWholeMonths() {
		assertEquals(new PetAge("age.months", 5), PetAge.between(LocalDate.of(2026, 6, 1), TODAY));
	}

	@Test
	void petOneDayShortOfAMonthIsUnderAMonth() {
		assertEquals(new PetAge("age.underAMonth", 0), PetAge.between(LocalDate.of(2026, 10, 21), TODAY));
	}

	@Test
	void petBornTodayIsUnderAMonth() {
		assertEquals(new PetAge("age.underAMonth", 0), PetAge.between(TODAY, TODAY));
	}

	@Test
	void petBornOnALeapDayTurnsOneOnTheFirstOfMarchInANonLeapYear() {
		LocalDate leapDay = LocalDate.of(2024, 2, 29);

		assertEquals(new PetAge("age.months", 11), PetAge.between(leapDay, LocalDate.of(2025, 2, 28)));
		assertEquals(new PetAge("age.year", 1), PetAge.between(leapDay, LocalDate.of(2025, 3, 1)));
	}

	@Test
	void petWithoutBirthDateHasNoAge() {
		assertNull(PetAge.between(null, TODAY));
	}

	@Test
	void petBornAfterTodayHasNoAge() {
		assertNull(PetAge.between(TODAY.plusDays(1), TODAY));
	}

}
