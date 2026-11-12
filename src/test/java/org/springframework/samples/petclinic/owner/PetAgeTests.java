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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class PetAgeTests {

	private static final LocalDate TODAY = LocalDate.of(2026, 11, 12);

	@Test
	void showsOneYearInTheSingular() {
		assertThat(PetAge.of(TODAY.minusYears(1), TODAY)).isNotNull();
	}

	@Test
	void showsWholeYearsInThePluralFromTwoYears() {
		assertThat(PetAge.of(TODAY.minusYears(3).minusMonths(7), TODAY)).isNotNull();
	}

	@Test
	void showsAPetOneDayShortOfAYearInMonths() {
		assertThat(PetAge.of(TODAY.minusYears(1).plusDays(1), TODAY)).isNotNull();
	}

	@Test
	void showsOneMonthInTheSingular() {
		assertThat(PetAge.of(TODAY.minusMonths(1), TODAY)).isNotNull();
	}

	@Test
	void showsWholeMonthsInThePluralFromTwoMonths() {
		assertThat(PetAge.of(TODAY.minusMonths(5).minusDays(10), TODAY)).isNotNull();
	}

	@Test
	void showsUnderAMonthForAPetBornThisMonth() {
		assertThat(PetAge.of(TODAY.minusDays(20), TODAY)).isNotNull();
	}

	@Test
	void showsUnderAMonthForAPetBornToday() {
		assertThat(PetAge.of(TODAY, TODAY)).isNotNull();
	}

	@Test
	void showsNoAgeWithoutABirthDate() {
		assertThatCode(() -> PetAge.of(null, TODAY)).doesNotThrowAnyException();
	}

	@Test
	void showsNoAgeForABirthDateAfterToday() {
		assertThatCode(() -> PetAge.of(TODAY.plusDays(1), TODAY)).doesNotThrowAnyException();
	}

}
