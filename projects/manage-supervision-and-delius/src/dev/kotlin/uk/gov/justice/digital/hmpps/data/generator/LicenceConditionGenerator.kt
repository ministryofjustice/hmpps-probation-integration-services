package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.ACTIVE_ORDER
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionMainCategory
import java.time.LocalDate

object LicenceConditionGenerator {

    val LONG_NOTE = """
        Licence Condition created automatically from the Create and Vary a licence system of\nAllow person(s) as designated by your supervising officer to install an electronic monitoring tag on you and access to install any associated equipment in your property, and for the purpose of ensuring that equipment is functioning correctly. You must not damage or tamper with these devices and ensure that the tag is charged, and report to your supervising officer and the EM provider immediately if the tag or the associated equipment are not working correctly. This will be for the purpose of monitoring your alcohol abstinence licence condition(s) unless otherwise authorised by your supervising officer. Licence Condition created automatically from the Create and Vary a licence system of\nAllow person(s) as designated by your supervising officer to install an electronic monitoring tag on you and access to install any associated equipment in your property, and for the purpose of ensuring that equipment is functioning correctly. You must not damage or tamper with these devices and ensure that the tag is charged, and report to your supervising officer and the EM provider immediately if the tag or the associated equipment are not working correctly. This will be for the purpose of monitoring your alcohol abstinence licence condition(s) unless otherwise authorised by your supervising officer.Licence Condition created automatically from the Create and Vary a licence system of\nAllow person(s) as desi123456
    """.trimIndent()

    val NOTE_1500_CHARS = """
        Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna. Sed consequat, leo eget bibendum sodales, augue velit cursus nunc, quis gravida magna mi a libero. Fusce vulputate eleifend sapien. Vestibulum purus quam, scelerisque ut, mollis sed, nonummy id, met
    """.trimIndent()

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
            $LONG_NOTE
            ---------------------------------------------------------
            Comment added by Joe Root on 23/04/2024 at 13:45
            You must not drink any alcohol until Wednesday 7th August 2024 unless your
            probation officer says you can. You will need to wear an electronic tag all the time so
            we can check this.
        """.trimIndent()
    )

    val LC_WITH_NOTES_WITHOUT_ADDED_BY = LicenceCondition(
        IdGenerator.getAndIncrement(),
        LIC_COND_MAIN_CAT,
        LIC_COND_SUB_CAT,
        ACTIVE_ORDER.id,
        LocalDate.now().minusDays(7),
        LocalDate.now(),
        """
            He shall not contact or associate with Peter Jones without the prior approval of the supervising officer;
        """.trimIndent()
    )

    val LC_WITH_1500_CHAR_NOTE = LicenceCondition(
        IdGenerator.getAndIncrement(),
        LIC_COND_MAIN_CAT,
        LIC_COND_SUB_CAT,
        ACTIVE_ORDER.id,
        LocalDate.now().minusDays(7),
        LocalDate.now(),
        """
            Comment added by Harry Kane on 29/10/2024 at 14:39
            $NOTE_1500_CHARS
            ---------------------------------------------------------
            Comment added by Tom Brady on 29/10/2024 at 14:56
            Needs to stay home every evening
        """.trimIndent()
    )
}