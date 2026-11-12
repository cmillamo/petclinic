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
 * A pet's age as shown on the owner's page: whole years, else whole months, else under a
 * month. {@code key} is the message key of the text, {@code count} its argument.
 */
public record PetAge(String key, int count) {

	/**
	 * Returns the age on {@code today}, or {@code null} if there is no birth date or it
	 * lies after {@code today}.
	 */
	public static PetAge of(LocalDate birthDate, LocalDate today) {
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
		return new PetAge("age.underMonth", 0);
	}

}
