package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.service.entity.Caseload
import uk.gov.justice.digital.hmpps.service.entity.Person
import uk.gov.justice.digital.hmpps.service.entity.Staff

object CaseloadGenerator {
    fun generateCaseload(person: Person, staff: Staff, id: Long = IdGenerator.getAndIncrement()) =
        Caseload(person, staff, id)
}