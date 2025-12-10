package uk.gov.justice.digital.hmpps.entity.sentence.component.manager

import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team

interface Manager {
    val staff: Staff
    val team: Team
}

