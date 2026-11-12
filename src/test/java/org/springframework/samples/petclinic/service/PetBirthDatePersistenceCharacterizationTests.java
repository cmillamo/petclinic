package org.springframework.samples.petclinic.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetTypeRepository;

/**
 * Characterization tests: how a pet's birth date is stored and read back today.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PetBirthDatePersistenceCharacterizationTests {

	@Autowired
	private OwnerRepository owners;

	@Autowired
	private PetTypeRepository types;

	@Test
	void seededPetHasItsBirthDate() {
		Owner george = this.owners.findById(1).get();
		assertThat(george.getPet("Leo").getBirthDate()).isEqualTo(LocalDate.of(2010, 9, 7));
	}

	@Test
	void savedBirthDateIsReadBackUnchanged() {
		Owner george = this.owners.findById(1).get();
		Pet pet = new Pet();
		pet.setName("bowser");
		pet.setType(this.types.findPetTypes().iterator().next());
		pet.setBirthDate(LocalDate.of(2024, 2, 29));
		george.addPet(pet);
		this.owners.saveAndFlush(george);

		assertThat(this.owners.findById(1).get().getPet("bowser").getBirthDate()).isEqualTo(LocalDate.of(2024, 2, 29));
	}

	@Test
	void databaseAcceptsPetWithoutBirthDate() {
		// The birth_date column is nullable; only the web validator requires a value.
		Owner george = this.owners.findById(1).get();
		Pet pet = new Pet();
		pet.setName("nodate");
		pet.setType(this.types.findPetTypes().iterator().next());
		george.addPet(pet);
		this.owners.saveAndFlush(george);

		assertThat(this.owners.findById(1).get().getPet("nodate").getBirthDate()).isNull();
	}

}
