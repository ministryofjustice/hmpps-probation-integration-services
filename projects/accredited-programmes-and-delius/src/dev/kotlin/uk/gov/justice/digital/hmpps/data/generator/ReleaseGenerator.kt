package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.custody.Custody
import uk.gov.justice.digital.hmpps.entity.sentence.custody.Release
import java.time.LocalDate
import java.time.ZonedDateTime

object ReleaseGenerator {
    fun generate(custody: Custody, type: ReferenceData) = Release(
        id = id(),
        custody = custody,
        date = LocalDate.of(2010, 1, 1),
        type = type,
        createdDateTime = ZonedDateTime.of(2010, 1, 1, 12, 0, 0, 0, EuropeLondon),
        softDeleted = false
    )
}
