package uk.gov.justice.digital.hmpps.api.model.activity

import uk.gov.justice.digital.hmpps.api.model.PersonSummary

data class PersonActivity(
    val personSummary: PersonSummary,
    val activities: List<Activity>
) 