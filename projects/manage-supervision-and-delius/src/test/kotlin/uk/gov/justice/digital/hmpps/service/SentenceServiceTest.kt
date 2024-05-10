package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.api.model.overview.Order
import uk.gov.justice.digital.hmpps.api.model.overview.Rar
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.api.model.sentence.AdditionalSentence
import uk.gov.justice.digital.hmpps.data.generator.AdditionalSentenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.CourtAppearanceGenerator
import uk.gov.justice.digital.hmpps.data.generator.CourtGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.CourtDocumentDetails
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class SentenceServiceTest {

    @Mock
    lateinit var eventRepository: EventSentenceRepository

    @Mock
    lateinit var courtAppearanceRepository: CourtAppearanceRepository

    @Mock
    lateinit var additionalSentenceRepository: AdditionalSentenceRepository

    @Mock
    lateinit var requirementRepository: RequirementRepository

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var documentRepository: DocumentRepository

    @Mock
    lateinit var offenderManagerRepository: OffenderManagerRepository

    @Mock
    lateinit var upwAppointmentRepository: UpwAppointmentRepository

    @InjectMocks
    lateinit var service: SentenceService

    @Test
    fun `no active sentences`() {

        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)
        whenever(eventRepository.findSentencesByPersonId(PersonGenerator.OVERVIEW.id)).thenReturn(
            listOf()
        )
        whenever(offenderManagerRepository.countOffenderManagersByPerson(PersonGenerator.OVERVIEW)).thenReturn(
            0
        )

        val expected =
            SentenceOverview(
                PersonGenerator.OVERVIEW.toSummary(),
                listOf(), ProbationHistory(0, null, 0, 0)
            )
        val response = service.getEvents(PersonGenerator.OVERVIEW.crn)

        assertEquals(expected, response)

        verify(personRepository, times(1)).findByCrn(PersonGenerator.OVERVIEW.crn)
        verify(eventRepository, times(1)).findSentencesByPersonId(PersonGenerator.OVERVIEW.id)

        verifyNoMoreInteractions(eventRepository)
        verifyNoMoreInteractions(personRepository)
        verifyNoInteractions(courtAppearanceRepository)
        verifyNoInteractions(additionalSentenceRepository)
        verifyNoInteractions(requirementRepository)
        verifyNoInteractions(documentRepository)
        verifyNoInteractions(upwAppointmentRepository)
    }

    @Test
    fun `recent active sentences`() {

        val event = PersonGenerator.generateEvent(
            person = PersonGenerator.OVERVIEW,
            active = true,
            inBreach = true,
            disposal = PersonGenerator.ACTIVE_ORDER,
            eventNumber = "123457",
            mainOffence = PersonGenerator.MAIN_OFFENCE_1,
            notes = "overview",
            additionalOffences = listOf(PersonGenerator.ADDITIONAL_OFFENCE_1)
        )

        val requirement1 = RequirementDetails(
            1,
            LocalDate.now().minusDays(21),
            LocalDate.now(),
            LocalDate.now().minusDays(14),
            LocalDate.now().minusDays(7),
            LocalDate.now().minusDays(3),
            "Expired (Normal)",
            12,
            "Weeks",
            "W",
            "Drug Rehabilitation",
            "Medium Intensity",
            "new requirement"
        )
        val requirement2 = RequirementDetails(
            2,
            null,
            LocalDate.now(),
            null,
            null,
            null,
            null,
            null,
            null,
            "F",
            "Main",
            "High Intensity",
            "rar requirement"
        )

        val requirement3 = RequirementDetails(
            1,
            LocalDate.now(),
            LocalDate.now(),
            null,
            LocalDate.now().minusDays(7),
            null,
            "Expired (Normal)",
            null,
            null,
            "RM49",
            "Curfew (Electronic Monitored)",
            null,
            "new requirement"
        )

        val courtDocumentDetails = CourtDocs("A001", LocalDate.now(), "Pre Sentence Event")

        val completedRarDays = OverviewServiceTest.RarDays(1, "COMPLETED")

        val scheduledRarDays = OverviewServiceTest.RarDays(2, "SCHEDULED")

        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)

        whenever(eventRepository.findSentencesByPersonId(PersonGenerator.OVERVIEW.id)).thenReturn(listOf(event))

        whenever(courtAppearanceRepository.getFirstCourtAppearanceByEventIdOrderByDate(event.id))
            .thenReturn(CourtAppearanceGenerator.generate(CourtGenerator.DEFAULT))

        whenever(additionalSentenceRepository.getAllByEventId(event.id))
            .thenReturn(listOf(AdditionalSentenceGenerator.SENTENCE_DISQ, AdditionalSentenceGenerator.SENTENCE_FINE))

        whenever(requirementRepository.getRequirements(event.id, event.eventNumber))
            .thenReturn(listOf(requirement1, requirement2, requirement3))

        whenever(requirementRepository.getRarDaysByRequirementId(requirement2._id)).thenReturn(
            listOf(
                completedRarDays,
                scheduledRarDays
            )
        )

        whenever(documentRepository.getCourtDocuments(event.id, event.eventNumber)).thenReturn(
            listOf(
                courtDocumentDetails
            )
        )

        whenever(upwAppointmentRepository.calculateUnpaidTimeWorked(event.id, event.eventNumber)).thenReturn(3936)

        val response = service.getEvents(PersonGenerator.OVERVIEW.crn)

        val expected = SentenceOverview(
            PersonGenerator.OVERVIEW.toSummary(),
            listOf(
                Sentence(
                    OffenceDetails(
                        "123457",
                        Offence("Murder", 1),
                        LocalDate.now(),
                        "overview",
                        listOf(
                            Offence("Burglary", 1)
                        )
                    ),
                    Conviction(
                        "Hull Court",
                        null,
                        null,
                        listOf(
                            AdditionalSentence(3, null, null, "Disqualified from Driving"),
                            AdditionalSentence(null, 500, "fine notes", "Fine")
                        )
                    ),
                    Order("Default Sentence Type", 12, null, LocalDate.now().minusDays(14)),
                    listOf(
                        Requirement(
                            requirement1._code,
                            requirement1._expectedStartDate,
                            requirement1._startDate,
                            requirement1._expectedEndDate,
                            requirement1._terminationDate,
                            requirement1._terminationReason,
                            "${requirement1._description} - ${requirement1._codeDescription}",
                            requirement1._length,
                            requirement1.lengthUnitValue,
                            "17 hours and 36 minutes completed",
                            requirement1._notes,
                            null
                        ),
                        Requirement(
                            requirement2._code,
                            requirement2._expectedStartDate,
                            requirement2._startDate,
                            requirement2._expectedEndDate,
                            requirement2._terminationDate,
                            requirement2._terminationReason,
                            "3 days RAR, 1 completed",
                            requirement2._length,
                            requirement2.lengthUnitValue,
                            null,
                            requirement2._notes,
                            Rar(1, 2, 3)
                        ),
                        Requirement(
                            requirement3._code,
                            requirement3._expectedStartDate,
                            requirement3._startDate,
                            requirement3._expectedEndDate,
                            requirement3._terminationDate,
                            requirement3._terminationReason,
                            requirement3._description,
                            requirement3._length,
                            requirement3.lengthUnitValue,
                            null,
                            requirement3._notes,
                            null
                        )
                    ),
                    listOf(CourtDocument("A001", LocalDate.now(), "Pre Sentence Event"))
                )
            ),
            ProbationHistory(0, null, 0, 0)
        )

        assertEquals(expected, response)
        verify(eventRepository, times(1)).findSentencesByPersonId(PersonGenerator.OVERVIEW.id)
        verify(additionalSentenceRepository, times(1)).getAllByEventId(event.id)
        verify(courtAppearanceRepository, times(1)).getFirstCourtAppearanceByEventIdOrderByDate(event.id)
        verify(documentRepository, times(1)).getCourtDocuments(event.id, event.eventNumber)
        verify(upwAppointmentRepository, times(1)).calculateUnpaidTimeWorked(event.id, event.eventNumber)

        verifyNoMoreInteractions(eventRepository)
        verifyNoMoreInteractions(additionalSentenceRepository)
        verifyNoMoreInteractions(courtAppearanceRepository)
        verifyNoMoreInteractions(documentRepository)
        verifyNoMoreInteractions(upwAppointmentRepository)
    }

    data class RequirementDetails(
        val _id: Long,
        val _expectedStartDate: LocalDate?,
        val _startDate: LocalDate,
        val _commencementDate: LocalDate?,
        val _expectedEndDate: LocalDate?,
        val _terminationDate: LocalDate?,
        val _terminationReason: String?,
        val _length: Long?,
        val _lengthUnitValue: String?,
        val _code: String,
        val _description: String,
        val _codeDescription: String?,
        val _notes: String?
    ) : uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RequirementDetails {
        override val id: Long
            get() = _id

        override val expectedStartDate: LocalDate?
            get() = _expectedStartDate

        override val startDate: LocalDate
            get() = _startDate

        override val commencementDate: LocalDate?
            get() = _commencementDate

        override val expectedEndDate: LocalDate?
            get() = _expectedEndDate

        override val terminationDate: LocalDate?
            get() = _terminationDate

        override val terminationReason: String?
            get() = _terminationReason

        override val length: Long?
            get() = _length

        override val lengthUnitValue: String?
            get() = _lengthUnitValue

        override val code: String
            get() = _code

        override val description: String
            get() = _description

        override val codeDescription: String?
            get() = _codeDescription

        override val notes: String?
            get() = _notes
    }

    data class CourtDocs(
        val _id: String,
        val _lastSaved: LocalDate,
        val _documentName: String
    ) : CourtDocumentDetails {

        override val id: String
            get() = _id

        override val lastSaved: LocalDate
            get() = _lastSaved

        override val documentName: String
            get() = _documentName
    }
}