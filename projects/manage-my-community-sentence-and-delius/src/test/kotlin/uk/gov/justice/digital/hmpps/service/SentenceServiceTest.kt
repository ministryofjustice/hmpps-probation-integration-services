package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.Disposal
import uk.gov.justice.digital.hmpps.entity.sentence.DisposalType
import uk.gov.justice.digital.hmpps.entity.sentence.Event
import uk.gov.justice.digital.hmpps.entity.sentence.MainOffence
import uk.gov.justice.digital.hmpps.entity.sentence.Offence
import uk.gov.justice.digital.hmpps.entity.sentence.requirement.Requirement
import uk.gov.justice.digital.hmpps.entity.sentence.requirement.RequirementMainCategory
import uk.gov.justice.digital.hmpps.repository.ContactRepository
import uk.gov.justice.digital.hmpps.repository.EventRepository
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import uk.gov.justice.digital.hmpps.repository.UnpaidWorkAppointmentRepository
import java.time.LocalDate
import java.time.ZonedDateTime

internal class SentenceServiceTest {

    private val service = SentenceService(
        eventRepository = mock(),
        personRepository = mock(),
        contactRepository = mock(),
        unpaidWorkAppointmentRepository = mock(),
    )

    @Test
    fun `getSentenceProgress allocates UPW completion by requirement id`() {
        val personRepository = mock<PersonRepository>()
        val eventRepository = mock<EventRepository>()
        val contactRepository = mock<ContactRepository>()
        val unpaidWorkAppointmentRepository = mock<UnpaidWorkAppointmentRepository>()
        val sentenceService = SentenceService(
            eventRepository = eventRepository,
            personRepository = personRepository,
            contactRepository = contactRepository,
            unpaidWorkAppointmentRepository = unpaidWorkAppointmentRepository,
        )

        val event = mock<Event>()
        val disposal = mock<Disposal>()
        val mainOffence = mock<MainOffence>()
        val offence = mock<Offence>()

        whenever(personRepository.getIdByCrn("CRN")).thenReturn(10L)
        whenever(eventRepository.findByPersonIdAndDisposalNotNull(10L)).thenReturn(listOf(event))
        whenever(event.disposal).thenReturn(disposal)
        whenever(disposal.id).thenReturn(99L)
        whenever(disposal.type).thenReturn(DisposalType(1, "Sentence"))
        whenever(disposal.date).thenReturn(LocalDate.of(2024, 1, 1))
        whenever(disposal.expectedEndDate).thenReturn(LocalDate.of(2026, 1, 1))
        whenever(disposal.enteredExpectedEndDate).thenReturn(null)
        whenever(disposal.lastUpdatedDatetime).thenReturn(ZonedDateTime.parse("2024-01-02T00:00:00Z"))
        whenever(disposal.requirements).thenReturn(
            setOf(
                upwRequirement(1L, 3, disposal),
                upwRequirement(2L, 4, disposal),
                upwRequirement(3L, 5, disposal),
            )
        )
        whenever(disposal.licenceConditions).thenReturn(emptySet())
        whenever(disposal.custody).thenReturn(null)
        whenever(disposal.event).thenReturn(event)
        whenever(event.mainOffence).thenReturn(mainOffence)
        whenever(event.additionalOffences).thenReturn(emptySet())
        whenever(mainOffence.offence).thenReturn(offence)
        whenever(offence.code).thenReturn("M1")
        whenever(offence.description).thenReturn("Main offence")
        whenever(unpaidWorkAppointmentRepository.countHoursAttended(99L)).thenReturn(8)

        val response = sentenceService.getSentenceProgress("CRN")

        assertThat(
            response.sentences.single().requirements.map { it.completed },
            equalTo(listOf(3, 4, 1)),
        )
    }

    private fun upwRequirement(id: Long, length: Int, disposal: Disposal): Requirement {
        val mainCategory = RequirementMainCategory(1, "W", "Unpaid Work", ReferenceData(1, "H", "Hours"))
        val subCategory = ReferenceData(2, "W03", "Additional UPW Hours")

        return Requirement(
            id = id,
            length = length,
            disposal = disposal,
            mainCategory = mainCategory,
            subCategory = subCategory,
            imposedDate = LocalDate.of(2024, 1, 1),
            expectedStartDate = null,
            expectedEndDate = null,
            actualStartDate = null,
            actualEndDate = null,
            terminationReason = null,
            lastUpdatedDatetime = ZonedDateTime.parse("2024-01-02T00:00:00Z"),
        )
    }

    @Test
    fun `apportionHours assigns available hours to matching subtype code`() {
        val apportionedHours = service.apportionHours(
            upwSubTypes = listOf("UPW" to 10),
            totalUPWHours = 6,
        )

        assertThat(apportionedHours, equalTo(mapOf("UPW" to 6)))
    }

    @Test
    fun `apportionHours caps completed hours at requirement length`() {
        val apportionedHours = service.apportionHours(
            upwSubTypes = listOf("UPW" to 10),
            totalUPWHours = 12,
        )

        assertThat(apportionedHours, equalTo(mapOf("UPW" to 10)))
    }

    @Test
    fun `apportionHours allocates hours across multiple requirements in order`() {
        val apportionedHours = service.apportionHours(
            upwSubTypes = listOf("W03" to 8, "UPW" to 10, "W07" to 6),
            totalUPWHours = 14,
        )

        assertThat(apportionedHours, equalTo(mapOf("UPW" to 10, "W03" to 4)))
    }

    @Test
    fun `apportionHours sums allocations for repeated subtype codes`() {
        val apportionedHours = service.apportionHours(
            upwSubTypes = listOf("UPW" to 3, "UPW" to 4, "W03" to 5),
            totalUPWHours = 8,
        )

        assertThat(apportionedHours, equalTo(mapOf("UPW" to 7, "W03" to 1)))
    }

    @Test
    fun `apportionHours deducts W01 then W03 then W07 regardless of input order`() {
        val apportionedHours = service.apportionHours(
            upwSubTypes = listOf("W07" to 5, "W03" to 4, "W01" to 6),
            totalUPWHours = 8,
        )

        assertThat(apportionedHours, equalTo(mapOf("W01" to 6, "W03" to 2)))
    }

    @Test
    fun `apportionHours treats W06 as concurrent to sequential deductions`() {
        val apportionedHours = service.apportionHours(
            upwSubTypes = listOf("W06" to 4, "W03" to 8, "UPW" to 10),
            totalUPWHours = 12,
        )

        assertThat(apportionedHours, equalTo(mapOf("UPW" to 10, "W03" to 2, "W06" to 4)))
    }
}

