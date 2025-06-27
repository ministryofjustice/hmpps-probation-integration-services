package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DYNAMIC_RSR
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.STATIC_RSR
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.oasys.rsr.entity.RsrScoreHistory
import uk.gov.justice.digital.hmpps.integrations.delius.person.CaseEntity
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.ZonedDateTime

object RsrScoreHistoryGenerator {
    val HISTORY: List<RsrScoreHistory> = listOf(
        generate(1.1, STATIC_RSR, ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, EuropeLondon)),
        generate(2.2, DYNAMIC_RSR, ZonedDateTime.of(2025, 1, 2, 12, 0, 0, 0, EuropeLondon)),
        generate(3.3, STATIC_RSR, ZonedDateTime.of(2025, 1, 3, 12, 0, 0, 0, EuropeLondon)),
        generate(4.4, DYNAMIC_RSR, ZonedDateTime.of(2025, 1, 3, 12, 0, 0, 0, EuropeLondon)),
    )

    fun generate(
        score: Double,
        type: ReferenceData,
        date: ZonedDateTime = ZonedDateTime.now(),
        caseEntity: CaseEntity = CaseEntityGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement()
    ) = RsrScoreHistory(
        id = id,
        personId = caseEntity.id,
        score = score,
        reasonForChange = type,
        dateRecorded = date
    )
}
