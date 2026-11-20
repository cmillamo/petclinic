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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pins how the rendered owner details page shows each pet, including its age (PC-303).
 */
@WebMvcTest(OwnerController.class)
@DisabledInNativeImage
@DisabledInAotMode
class OwnerDetailsPageTests {

	private static final int OWNER_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	private Owner ownerWith(Pet... pets) {
		Owner owner = new Owner();
		owner.setId(OWNER_ID);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		owner.setAddress("110 W. Liberty St.");
		owner.setCity("Madison");
		owner.setTelephone("6085551023");
		int id = 1;
		for (Pet pet : pets) {
			owner.addPet(pet);
			pet.setId(id++);
		}
		given(this.owners.findById(OWNER_ID)).willReturn(Optional.of(owner));
		return owner;
	}

	private static Pet pet(String name, String type, LocalDate birthDate) {
		PetType petType = new PetType();
		petType.setName(type);
		Pet pet = new Pet();
		pet.setName(name);
		pet.setType(petType);
		pet.setBirthDate(birthDate);
		return pet;
	}

	@Test
	void eachPetShowsNameThenBirthDateThenAgeThenType() throws Exception {
		ownerWith(pet("Leo", "cat", LocalDate.of(2010, 9, 7)));

		mockMvc.perform(get("/owners/{ownerId}", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(content().string(stringContainsInOrder(
					List.of("<dt>Name</dt>", "<dd>Leo</dd>", "<dt>Birth Date</dt>", "<dd>2010-09-07</dd>",
							"<dt>Age</dt>", "<dd>", "</dd>", "<dt>Type</dt>", "<dd>cat</dd>", "</dl>"))));
	}

	@Test
	void petAYearOldOrMoreShowsItsAgeInWholeYears() throws Exception {
		LocalDate today = LocalDate.now();
		ownerWith(pet("Leo", "cat", today.minusYears(3).minusMonths(2)), pet("Max", "dog", today.minusYears(1)));

		mockMvc.perform(get("/owners/{ownerId}", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(content().string(stringContainsInOrder(List.of("<dd>Leo</dd>", "<dt>Age</dt>",
					"<dd>3 years</dd>", "<dd>Max</dd>", "<dt>Age</dt>", "<dd>1 year</dd>"))));
	}

	@Test
	void petUnderAYearOldShowsItsAgeInWholeMonths() throws Exception {
		LocalDate today = LocalDate.now();
		ownerWith(pet("Leo", "cat", today.minusMonths(5).minusDays(3)), pet("Max", "dog", today.minusMonths(1)));

		mockMvc.perform(get("/owners/{ownerId}", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(content().string(stringContainsInOrder(List.of("<dd>Leo</dd>", "<dt>Age</dt>",
					"<dd>5 months</dd>", "<dd>Max</dd>", "<dt>Age</dt>", "<dd>1 month</dd>"))));
	}

	@Test
	void petUnderAMonthOldShowsUnderAMonth() throws Exception {
		ownerWith(pet("Newborn", "cat", LocalDate.now()));

		mockMvc.perform(get("/owners/{ownerId}", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(content().string(stringContainsInOrder(
					List.of("<dd>Newborn</dd>", "<dt>Age</dt>", "<dd>under a month</dd>", "<dt>Type</dt>"))));
	}

	@Test
	void petWithoutBirthDateShowsTheAgeLabelWithAnEmptyValue() throws Exception {
		ownerWith(pet("Nameless", "dog", null));

		mockMvc.perform(get("/owners/{ownerId}", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(content().string(
					stringContainsInOrder(List.of("<dd>Nameless</dd>", "<dt>Age</dt>", "<dd></dd>", "<dt>Type</dt>"))));
	}

	@Test
	void petBornAfterTodayShowsTheAgeLabelWithAnEmptyValue() throws Exception {
		ownerWith(pet("Unborn", "dog", LocalDate.now().plusDays(1)));

		mockMvc.perform(get("/owners/{ownerId}", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(content().string(
					stringContainsInOrder(List.of("<dd>Unborn</dd>", "<dt>Age</dt>", "<dd></dd>", "<dt>Type</dt>"))));
	}

	@Test
	void ageLabelIsTranslatedButTheAgeTextStaysEnglish() throws Exception {
		ownerWith(pet("Leo", "cat", LocalDate.now().minusYears(3)));

		mockMvc.perform(get("/owners/{ownerId}", OWNER_ID).param("lang", "de"))
			.andExpect(status().isOk())
			.andExpect(content().string(stringContainsInOrder(List.of("<dt>Alter</dt>", "<dd>3 years</dd>"))));
	}

	@Test
	void petWithoutBirthDateRendersAnEmptyBirthDateCell() throws Exception {
		ownerWith(pet("Nameless", "dog", null));

		mockMvc.perform(get("/owners/{ownerId}", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(content().string(stringContainsInOrder(List.of("<dd>Nameless</dd>", "<dt>Birth Date</dt>",
					"<dd></dd>", "<dt>Type</dt>", "<dd>dog</dd>"))));
	}

	@Test
	void petBornInTheFutureIsShownWithItsFutureBirthDate() throws Exception {
		LocalDate nextYear = LocalDate.now().plusYears(1);
		ownerWith(pet("Unborn", "dog", nextYear));

		mockMvc.perform(get("/owners/{ownerId}", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("<dd>" + nextYear + "</dd>")));
	}

	@Test
	void petBornTodayIsShownWithTodaysDate() throws Exception {
		LocalDate today = LocalDate.now();
		ownerWith(pet("Newborn", "cat", today));

		mockMvc.perform(get("/owners/{ownerId}", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("<dd>" + today + "</dd>")));
	}

	@Test
	void everyPetOfTheOwnerGetsItsOwnBirthDate() throws Exception {
		ownerWith(pet("Leo", "cat", LocalDate.of(2010, 9, 7)), pet("Max", "dog", LocalDate.of(2012, 9, 4)));

		mockMvc.perform(get("/owners/{ownerId}", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(content().string(stringContainsInOrder(
					List.of("<dd>Leo</dd>", "<dd>2010-09-07</dd>", "<dd>Max</dd>", "<dd>2012-09-04</dd>"))));
	}

	@Test
	void ownerWithoutPetsShowsAnEmptyPetsTable() throws Exception {
		ownerWith();

		mockMvc.perform(get("/owners/{ownerId}", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Pets and Visits")))
			.andExpect(content().string(not(containsString("<dt>Birth Date</dt>"))));
	}

}
