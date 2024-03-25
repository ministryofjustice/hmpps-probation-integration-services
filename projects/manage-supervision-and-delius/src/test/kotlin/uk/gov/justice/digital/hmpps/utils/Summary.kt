package uk.gov.justice.digital.hmpps.utils

import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonSummaryEntity
import java.time.LocalDate

data class Summary(
    override val id: Long,
    override val forename: String,
    override val secondName: String? = null,
    override val thirdName: String? = null,
    override val surname: String,
    override val crn: String,
    override val pnc: String?,
    override val dateOfBirth: LocalDate
) : PersonSummaryEntity