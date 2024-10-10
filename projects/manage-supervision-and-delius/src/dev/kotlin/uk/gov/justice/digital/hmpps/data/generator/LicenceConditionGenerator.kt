package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.ACTIVE_ORDER
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionMainCategory
import java.time.LocalDate

object LicenceConditionGenerator {

    val LIC_COND_MAIN_CAT = LicenceConditionMainCategory(
        IdGenerator.getAndIncrement(),
        "LicMain",
        "lic cond main"
    )

    val LIC_COND_SUB_CAT = ReferenceData(
        IdGenerator.getAndIncrement(),
        "LicSub",
        "Lic Sub cat"
    )

    val LC_WITHOUT_NOTES = LicenceCondition(
        IdGenerator.getAndIncrement(),
        LIC_COND_MAIN_CAT,
        null,
        ACTIVE_ORDER.id,
        LocalDate.now().minusDays(14),
        null,
        null
    )

    val LC_WITH_NOTES = LicenceCondition(
        IdGenerator.getAndIncrement(),
        LIC_COND_MAIN_CAT,
        LIC_COND_SUB_CAT,
        ACTIVE_ORDER.id,
        LocalDate.now().minusDays(7),
        LocalDate.now(),
        "licence condition notes"
    )

}