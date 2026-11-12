package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Characterization tests: they record how a pet's birth date is entered, validated, saved
 * and shown today. They are not a statement of what it should do.
 */
@WebMvcTest(value = { PetController.class, OwnerController.class, VisitController.class },
		includeFilters = @ComponentScan.Filter(value = PetTypeFormatter.class, type = FilterType.ASSIGNABLE_TYPE))
@DisabledInNativeImage
@DisabledInAotMode
class PetBirthDateCharacterizationTests {

	private static final int OWNER_ID = 1;

	private static final int PET_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	@MockitoBean
	private PetTypeRepository types;

	private Owner owner;

	private Pet petty;

	@BeforeEach
	void setup() {
		PetType hamster = new PetType();
		hamster.setId(3);
		hamster.setName("hamster");
		given(this.types.findPetTypes()).willReturn(List.of(hamster));

		owner = new Owner();
		owner.setId(OWNER_ID);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		owner.setAddress("110 W. Liberty St.");
		owner.setCity("Madison");
		owner.setTelephone("6085551023");
		petty = new Pet();
		petty.setName("petty");
		petty.setType(hamster);
		petty.setBirthDate(LocalDate.of(2015, 2, 12));
		owner.addPet(petty);
		petty.setId(PET_ID);
		given(this.owners.findById(OWNER_ID)).willReturn(Optional.of(owner));
	}

	// ---- entering and checking: create ----

	@Test
	void createAcceptsTodayAndSavesIt() throws Exception {
		LocalDate today = LocalDate.now();
		mockMvc
			.perform(post("/owners/{id}/pets/new", OWNER_ID).param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", today.toString()))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));

		ArgumentCaptor<Owner> saved = ArgumentCaptor.forClass(Owner.class);
		verify(owners).saveAndFlush(saved.capture());
		assertThat(saved.getValue().getPet("Betty").getBirthDate()).isEqualTo(today);
	}

	@Test
	void createRejectsTomorrowWithTypeMismatchBirthDate() throws Exception {
		mockMvc
			.perform(post("/owners/{id}/pets/new", OWNER_ID).param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", LocalDate.now().plusDays(1).toString()))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"))
			.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "typeMismatch.birthDate"));
		verify(owners, never()).saveAndFlush(any());
	}

	@Test
	void createAcceptsYesterday() throws Exception {
		mockMvc
			.perform(post("/owners/{id}/pets/new", OWNER_ID).param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", LocalDate.now().minusDays(1).toString()))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	void createAcceptsVeryOldDate() throws Exception {
		mockMvc
			.perform(post("/owners/{id}/pets/new", OWNER_ID).param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "1900-01-01"))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	void createWithoutBirthDateIsRequired() throws Exception {
		mockMvc.perform(post("/owners/{id}/pets/new", OWNER_ID).param("name", "Betty").param("type", "hamster"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"))
			.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "required"));
		verify(owners, never()).saveAndFlush(any());
	}

	@Test
	void createWithEmptyBirthDateIsRequired() throws Exception {
		mockMvc
			.perform(post("/owners/{id}/pets/new", OWNER_ID).param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", ""))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "required"));
	}

	@Test
	void createWithUnparseableBirthDateIsTypeMismatch() throws Exception {
		mockMvc
			.perform(post("/owners/{id}/pets/new", OWNER_ID).param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "12-02-2015"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "typeMismatch"));
	}

	// ---- entering and checking: update ----

	@Test
	void updateRejectsFutureBirthDateAndKeepsOldDate() throws Exception {
		mockMvc
			.perform(post("/owners/{id}/pets/{petId}/edit", OWNER_ID, PET_ID).param("name", "petty")
				.param("type", "hamster")
				.param("birthDate", LocalDate.now().plusYears(1).toString()))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "typeMismatch.birthDate"));
		verify(owners, never()).saveAndFlush(any());
	}

	@Test
	void updateCopiesBirthDateToExistingPet() throws Exception {
		mockMvc
			.perform(post("/owners/{id}/pets/{petId}/edit", OWNER_ID, PET_ID).param("name", "petty")
				.param("type", "hamster")
				.param("birthDate", "2020-06-30"))
			.andExpect(status().is3xxRedirection());
		assertThat(owner.getPet(PET_ID).getBirthDate()).isEqualTo(LocalDate.of(2020, 6, 30));
	}

	@Test
	void updateWithoutBirthDateParameterKeepsExistingDate() throws Exception {
		// The bound pet is the stored one, so an omitted parameter leaves its date as is.
		mockMvc
			.perform(post("/owners/{id}/pets/{petId}/edit", OWNER_ID, PET_ID).param("name", "petty")
				.param("type", "hamster"))
			.andExpect(status().is3xxRedirection());
		assertThat(owner.getPet(PET_ID).getBirthDate()).isEqualTo(LocalDate.of(2015, 2, 12));
		verify(owners).saveAndFlush(owner);
	}

	@Test
	void updateWithEmptyBirthDateIsRequiredAndNothingSaved() throws Exception {
		mockMvc
			.perform(post("/owners/{id}/pets/{petId}/edit", OWNER_ID, PET_ID).param("name", "petty")
				.param("type", "hamster")
				.param("birthDate", ""))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"))
			.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "required"));
		verify(owners, never()).saveAndFlush(any());
	}

	@Test
	void updateWithUnparseableBirthDateIsTypeMismatchAndKeepsOldDate() throws Exception {
		mockMvc
			.perform(post("/owners/{id}/pets/{petId}/edit", OWNER_ID, PET_ID).param("name", "petty")
				.param("type", "hamster")
				.param("birthDate", "2015/02/12"))
			.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "typeMismatch"));
		assertThat(owner.getPet(PET_ID).getBirthDate()).isEqualTo(LocalDate.of(2015, 2, 12));
	}

	// ---- showing ----

	@Test
	void editFormShowsBirthDateAsDateInput() throws Exception {
		mockMvc.perform(get("/owners/{id}/pets/{petId}/edit", OWNER_ID, PET_ID))
			.andExpect(status().isOk())
			.andExpect(content().string(org.hamcrest.Matchers.containsString("type=\"date\"")))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"2015-02-12\"")));
	}

	@Test
	void ownerPageShowsBirthDateAsIsoText() throws Exception {
		mockMvc.perform(get("/owners/{id}", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownerDetails"))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("2015-02-12")));
	}

	@Test
	void visitFormShowsBirthDateAsIsoText() throws Exception {
		mockMvc.perform(get("/owners/{id}/pets/{petId}/visits/new", OWNER_ID, PET_ID))
			.andExpect(status().isOk())
			.andExpect(content().string(org.hamcrest.Matchers.containsString("2015-02-12")));
	}

}
