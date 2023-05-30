package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.courtreport.CourtReport
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object CourtReportGenerator {
    val DEFAULT = generate(PersonGenerator.DEFAULT)

    fun generate(person: Person, id: Long = IdGenerator.getAndIncrement()) = CourtReport(id, person)
}
