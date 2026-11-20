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

class PetTests {

	@Test
	void newPetHasNoBirthDate() {
		assertNull(new Pet().getBirthDate());
	}

	@Test
	void birthDateIsReturnedExactlyAsSet() {
		Pet pet = new Pet();
		LocalDate leapDay = LocalDate.of(2020, 2, 29);

		pet.setBirthDate(leapDay);

		assertEquals(leapDay, pet.getBirthDate());
	}

	@Test
	void futureBirthDateIsAcceptedBySetter() {
		Pet pet = new Pet();
		LocalDate tomorrow = LocalDate.now().plusDays(1);

		pet.setBirthDate(tomorrow);

		assertEquals(tomorrow, pet.getBirthDate());
	}

}
