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
        """
            Comment added by CVL Service on 22/04/2024 at 10:00
            Licence Condition created automatically from the Create and Vary a licence system of
            Allow person(s) as designated by your supervising officer to install an electronic monitoring tag on you and access to install any associated equipment in your property, and for the purpose of ensuring that equipment is functioning correctly. You must not damage or tamper with these devices and ensure that the tag is charged, and report to your supervising officer and the EM provider immediately if the tag or the associated equipment are not working correctly. This will be for the purpose of monitoring your alcohol abstinence licence condition(s) unless otherwise authorised by your supervising officer.
            ---------------------------------------------------------
            Comment added by Joe Root on 23/04/2024 at 13:45
            You must not drink any alcohol until Wednesday 7th August 2024 unless your
            probation officer says you can. You will need to wear an electronic tag all the time so
            we can check this.
        """.trimIndent()
    )

}