package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.overview.Order
import uk.gov.justice.digital.hmpps.api.model.overview.Rar
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.data.generator.AdditionalSentenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.CourtAppearanceGenerator
import uk.gov.justice.digital.hmpps.data.generator.CourtGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.CourtDocumentDetails
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.PersonalContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AdditionalSentenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CourtAppearanceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.EventSentenceRepository
import uk.gov.justice.digital.hmpps.utils.Summary
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
    lateinit var personalContactRepository: PersonalContactRepository

    @InjectMocks
    lateinit var service: SentenceService

    private lateinit var personSummary: Summary

    @BeforeEach
    fun setup() {
        personSummary = Summary(
            id = 1,
            forename = "TestName",
            surname = "TestSurname", crn = "CRN", pnc = "PNC", dateOfBirth = LocalDate.now().minusYears(50)
        )
    }

    @Test
    fun `no active sentences`() {

        whenever(personRepository.findSummary(PersonGenerator.OVERVIEW.crn)).thenReturn(personSummary)
        whenever(eventRepository.findSentencesByPersonId(personSummary.id)).thenReturn(
            listOf()
        )

        val expected = SentenceOverview(Name("TestName", surname = "TestSurname"), listOf(), ProbationHistory(0, 0, 0))
        val response = service.getEvents(PersonGenerator.OVERVIEW.crn)

        assertEquals(expected, response)
        verify(personRepository, times(1)).findSummary(PersonGenerator.OVERVIEW.crn)
        verify(eventRepository, times(1)).findSentencesByPersonId(personSummary.id)

        verifyNoMoreInteractions(eventRepository)
        verifyNoMoreInteractions(personRepository)
        verifyNoInteractions(courtAppearanceRepository)
        verifyNoInteractions(additionalSentenceRepository)
        verifyNoInteractions(requirementRepository)
        verifyNoInteractions(documentRepository)
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

        val requirement = RequirementDetails(1, "Main", "High Intensity", 12, "new requirement")

        val courtDocumentDetails = CourtDocs(1, LocalDate.now(), "Pre Sentence Event")

        val completedRarDays = OverviewServiceTest.RarDays(1, "COMPLETED")

        val scheduledRarDays = OverviewServiceTest.RarDays(2, "SCHEDULED")

        whenever(personRepository.findSummary(PersonGenerator.OVERVIEW.crn)).thenReturn(personSummary)

        whenever(eventRepository.findSentencesByPersonId(personSummary.id)).thenReturn(listOf(event))

        whenever(courtAppearanceRepository.getFirstCourtAppearanceByEventIdOrderByDate(event.id))
            .thenReturn(CourtAppearanceGenerator.generate(CourtGenerator.DEFAULT))

        whenever(additionalSentenceRepository.getAllByEventId(event.id))
            .thenReturn(listOf(AdditionalSentenceGenerator.SENTENCE_DISQ, AdditionalSentenceGenerator.SENTENCE_FINE))

        whenever(requirementRepository.getRequirements(PersonGenerator.OVERVIEW.crn, event.eventNumber))
            .thenReturn(listOf(requirement))

        whenever(requirementRepository.getRarDaysByRequirementId(requirement._id)).thenReturn(
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

        val response = service.getEvents(PersonGenerator.OVERVIEW.crn)

        val expected = SentenceOverview(
            Name("TestName", surname = "TestSurname"),
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
                            requirement._description,
                            requirement._codeDescription,
                            requirement._length,
                            requirement._notes,
                            Rar(completedRarDays._days, scheduledRarDays._days, 3)
                        )
                    ),
                    listOf(CourtDocument(1, LocalDate.now(), "Pre Sentence Event"))
                )
            ),
            ProbationHistory(0, 0, 0)
        )

        assertEquals(expected, response)
        verify(eventRepository, times(1)).findSentencesByPersonId(personSummary.id)
        verify(additionalSentenceRepository, times(1)).getAllByEventId(event.id)
        verify(courtAppearanceRepository, times(1)).getFirstCourtAppearanceByEventIdOrderByDate(event.id)
        verify(documentRepository, times(1)).getCourtDocuments(event.id, event.eventNumber)

        verifyNoMoreInteractions(eventRepository)
        verifyNoMoreInteractions(additionalSentenceRepository)
        verifyNoMoreInteractions(courtAppearanceRepository)
        verifyNoMoreInteractions(documentRepository)
    }

    data class RequirementDetails(
        val _id: Long,
        val _description: String?,
        val _codeDescription: String?,
        val _length: Long?,
        val _notes: String?
    ) : uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RequirementDetails {
        override val id: Long
            get() = _id

        override val description: String?
            get() = _description

        override val codeDescription: String?
            get() = _codeDescription

        override val length: Long?
            get() = _length

        override val notes: String?
            get() = _notes
    }

    data class CourtDocs(
        val _id: Long,
        val _lastSaved: LocalDate,
        val _documentName: String
    ) : CourtDocumentDetails {

        override val id: Long
            get() = _id

        override val lastSaved: LocalDate
            get() = _lastSaved

        override val documentName: String
            get() = _documentName
    }
}