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
import java.time.Period;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PetAgeTests {

	private static final LocalDate TODAY = LocalDate.of(2026, 11, 12);

	private Period ageOf(LocalDate birthDate) {
		Pet pet = new Pet();
		pet.setBirthDate(birthDate);
		return pet.ageOn(TODAY);
	}

	@Test
	void countsWholeYearsForAPetOneYearOrOlder() {
		assertThat(ageOf(LocalDate.of(2025, 11, 12)).getYears()).isEqualTo(1);
		assertThat(ageOf(LocalDate.of(2023, 11, 13)).getYears()).isEqualTo(2);
	}

	@Test
	void countsWholeMonthsForAPetUnderAYear() {
		Period age = ageOf(LocalDate.of(2026, 6, 12));
		assertThat(age.getYears()).isZero();
		assertThat(age.getMonths()).isEqualTo(5);
	}

	@Test
	void countsZeroMonthsForAPetUnderAMonth() {
		Period age = ageOf(LocalDate.of(2026, 10, 20));
		assertThat(age.getYears()).isZero();
		assertThat(age.getMonths()).isZero();
	}

	@Test
	void hasNoAgeWithoutABirthDate() {
		assertThat(ageOf(null)).isNull();
	}

	@Test
	void hasNoAgeForABirthDateAfterToday() {
		assertThat(ageOf(TODAY.plusDays(1))).isNull();
	}

	@Test
	void hasAnAgeOnTheBirthDate() {
		assertThat(ageOf(TODAY)).isEqualTo(Period.ZERO);
	}

}
