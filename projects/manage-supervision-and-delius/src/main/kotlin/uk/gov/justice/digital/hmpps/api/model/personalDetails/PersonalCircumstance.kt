package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.overview.PersonalCircumstance
import java.time.LocalDate

data class Circumstances(
    val circumstances: List<PersonalCircumstance>,
    val lastUpdated: LocalDate?
)