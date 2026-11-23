package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.CaseloadRepository
import java.time.LocalDate
import java.time.ZonedDateTime

class CaseloadAppointmentIntegrationTest : IntegrationTestBase() {
    @Autowired
    lateinit var caseloadRepository: CaseloadRepository

    @Test
    @Transactional
    fun `appointments without start times use midnight for selection and pagination`() {
        val person = PersonGenerator.OVERVIEW
        val type = ContactGenerator.APPT_CT_2
        val today = LocalDate.now(EuropeLondon)

        entityManager.createNativeQuery("update contact set soft_deleted = 1 where offender_id = :personId")
            .setParameter("personId", person.id)
            .executeUpdate()

        fun appointment(date: LocalDate, hour: Int?, startTimeMissing: Boolean = false) =
            ContactGenerator.generateContact(
                person,
                type,
                date.atTime(hour ?: 0, 0).atZone(EuropeLondon),
                startTime = if (startTimeMissing) null else date.atTime(hour ?: 0, 0).atZone(EuropeLondon)
            )

        val tomorrowMidnight = appointment(today.plusDays(1), null, startTimeMissing = true)
        val tomorrowMorning = appointment(today.plusDays(1), 9)
        val yesterdayMidnight = appointment(today.minusDays(1), null, startTimeMissing = true)
        val yesterdayMorning = appointment(today.minusDays(1), 9)
        contactRepository.saveAll(listOf(tomorrowMidnight, tomorrowMorning, yesterdayMidnight, yesterdayMorning))
        entityManager.flush()

        fun caseload() = caseloadRepository.searchByStaffId(
            PageRequest.of(0, 1),
            ContactGenerator.DEFAULT_STAFF.id,
            person.crn.lowercase(),
            type.code
        )

        val page = caseload()
        val item = page.content.single()
        assertThat(page.totalElements).isEqualTo(1)
        assertThat(item.nextAppointmentId).isEqualTo(tomorrowMidnight.id)
        assertThat(item.nextAppointmentDateTime).isEqualTo(today.plusDays(1).atStartOfDay())
        assertThat(item.prevAppointmentId).isEqualTo(yesterdayMorning.id)
        assertThat(item.prevAppointmentDateTime).isEqualTo(today.minusDays(1).atTime(9, 0))
        assertThat(caseloadRepository.findContactTypesForStaff(ContactGenerator.DEFAULT_STAFF.id).map { it.code })
            .contains(type.code)

        entityManager.createNativeQuery("update contact set soft_deleted = 1 where contact_id = :id")
            .setParameter("id", yesterdayMorning.id)
            .executeUpdate()
        val previousWithoutTime = caseload().content.single()
        assertThat(previousWithoutTime.prevAppointmentId).isEqualTo(yesterdayMidnight.id)
        assertThat(previousWithoutTime.prevAppointmentDateTime).isEqualTo(today.minusDays(1).atStartOfDay())

        val todayMidnight = appointment(today, null, startTimeMissing = true)
        contactRepository.save(todayMidnight)
        entityManager.flush()
        val sameDay = caseload().content.single()
        assertThat(sameDay.nextAppointmentId).isEqualTo(tomorrowMidnight.id)
        assertThat(sameDay.prevAppointmentId).isEqualTo(todayMidnight.id)
        assertThat(sameDay.prevAppointmentDateTime).isEqualTo(today.atStartOfDay())

        entityManager.createNativeQuery(
            "update disposal set active_flag = 0 where event_id in (select event_id from event where offender_id = :personId)"
        ).setParameter("personId", person.id).executeUpdate()
        val withoutSentence = caseload()
        assertThat(withoutSentence.content).hasSize(1)
        assertThat(withoutSentence.totalElements).isEqualTo(1)
    }
}
