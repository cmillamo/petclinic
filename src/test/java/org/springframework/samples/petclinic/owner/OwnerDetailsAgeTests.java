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
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(OwnerController.class)
@DisabledInAotMode
class OwnerDetailsAgeTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	private String pageForPetBornOn(LocalDate birthDate) throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		Pet pet = new Pet();
		PetType dog = new PetType();
		dog.setName("dog");
		pet.setType(dog);
		pet.setName("Max");
		pet.setBirthDate(birthDate);
		owner.addPet(pet);
		pet.setId(1);
		given(this.owners.findById(1)).willReturn(Optional.of(owner));
		return this.mockMvc.perform(get("/owners/1")).andReturn().getResponse().getContentAsString();
	}

	private String ageEntry(String page) {
		String afterLabel = page.substring(page.indexOf("<dt>Age</dt>"));
		String dd = afterLabel.substring(afterLabel.indexOf("<dd"), afterLabel.indexOf("</dd>"));
		return dd.replaceAll("<[^>]*>", "").trim();
	}

	@Test
	void showsAgeInWholeYearsForAPetOverAYearOld() throws Exception {
		assertThat(ageEntry(pageForPetBornOn(LocalDate.now().minusYears(3)))).isEqualTo("3 years");
	}

	@Test
	void showsOneYearInTheSingular() throws Exception {
		assertThat(ageEntry(pageForPetBornOn(LocalDate.now().minusYears(1)))).isEqualTo("1 year");
	}

	@Test
	void showsAgeInWholeMonthsForAPetUnderAYearOld() throws Exception {
		assertThat(ageEntry(pageForPetBornOn(LocalDate.now().minusMonths(5)))).isEqualTo("5 months");
		assertThat(ageEntry(pageForPetBornOn(LocalDate.now().minusMonths(1)))).isEqualTo("1 month");
	}

	@Test
	void showsUnderAMonthForAPetBornThisMonth() throws Exception {
		assertThat(ageEntry(pageForPetBornOn(LocalDate.now()))).isEqualTo("under a month");
	}

	@Test
	void showsAnEmptyAgeForAPetWithoutABirthDate() throws Exception {
		assertThat(ageEntry(pageForPetBornOn(null))).isEmpty();
	}

	@Test
	void showsAnEmptyAgeForAPetBornAfterToday() throws Exception {
		assertThat(ageEntry(pageForPetBornOn(LocalDate.now().plusDays(3)))).isEmpty();
	}

}
