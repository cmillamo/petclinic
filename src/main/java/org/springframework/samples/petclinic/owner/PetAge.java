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

/**
 * A pet's age as shown to users: the message key to display and the number it takes.
 * Worked out on demand, never stored.
 */
public record PetAge(String messageKey, int amount) {

	/**
	 * @return the age on {@code today}, or {@code null} if the birth date is unknown or
	 * after {@code today}
	 */
	static PetAge between(LocalDate birthDate, LocalDate today) {
		if (birthDate == null || birthDate.isAfter(today)) {
			return null;
		}
		Period period = Period.between(birthDate, today);
		if (period.getYears() > 0) {
			return new PetAge(period.getYears() == 1 ? "age.year" : "age.years", period.getYears());
		}
		if (period.getMonths() > 0) {
			return new PetAge(period.getMonths() == 1 ? "age.month" : "age.months", period.getMonths());
		}
		return new PetAge("age.underAMonth", 0);
	}

}
