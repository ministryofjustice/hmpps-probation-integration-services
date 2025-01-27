package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.api.model.overview.Order
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.api.model.sentence.AdditionalSentence
import uk.gov.justice.digital.hmpps.data.generator.AdditionalSentenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.CourtAppearanceGenerator
import uk.gov.justice.digital.hmpps.data.generator.CourtGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.REQUIREMENT
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

    @Mock
    lateinit var licenceConditionRepository: LicenceConditionRepository

    @Mock
    lateinit var custodyRepository: CustodyRepository

    @Mock
    lateinit var requirementService: RequirementService

    @InjectMocks
    lateinit var service: SentenceService

    private val event = PersonGenerator.generateEvent(
        person = PersonGenerator.OVERVIEW,
        active = true,
        inBreach = true,
        disposal = PersonGenerator.ACTIVE_ORDER,
        eventNumber = "123457",
        mainOffence = PersonGenerator.MAIN_OFFENCE_1,
        notes = "overview",
        additionalOffences = listOf(PersonGenerator.ADDITIONAL_OFFENCE_1)
    )

    private val requirement1 = REQUIREMENT

    @Test
    fun `no active sentences`() {

        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)
        whenever(eventRepository.findSentencesByPersonId(PersonGenerator.OVERVIEW.id)).thenReturn(
            listOf()
        )

        val expected =
            SentenceOverview(
                PersonGenerator.OVERVIEW.toSummary(),
                emptyList()
            )
        val response = service.getEvents(PersonGenerator.OVERVIEW.crn, null)

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

        val courtDocumentDetails = CourtDocs("A001", LocalDate.now(), "Pre Sentence Event")

        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)

        whenever(eventRepository.findSentencesByPersonId(PersonGenerator.OVERVIEW.id)).thenReturn(listOf(event))

        whenever(courtAppearanceRepository.getFirstCourtAppearanceByEventIdOrderByDate(event.id))
            .thenReturn(CourtAppearanceGenerator.generate(CourtGenerator.DEFAULT))

        whenever(additionalSentenceRepository.getAllByEventId(event.id))
            .thenReturn(listOf(AdditionalSentenceGenerator.SENTENCE_DISQ, AdditionalSentenceGenerator.SENTENCE_FINE))

        whenever(requirementRepository.getRequirements(event.id, event.eventNumber))
            .thenReturn(listOf(requirement1))

        whenever(documentRepository.getCourtDocuments(event.id, event.eventNumber)).thenReturn(
            listOf(
                courtDocumentDetails
            )
        )

        whenever(requirementRepository.sumTotalUnpaidWorkHoursByDisposal(event.disposal!!.id)).thenReturn(70)
        whenever(upwAppointmentRepository.calculateUnpaidTimeWorked(event.disposal!!.id)).thenReturn(3936)

        whenever(requirementService.getRar(requirement1.disposal!!.id, requirement1.mainCategory!!.code)).thenReturn(null)

        val response = service.getEvents(PersonGenerator.OVERVIEW.crn, null)

        val expected = SentenceOverview(
            PersonGenerator.OVERVIEW.toSummary(),
            listOf(SentenceSummary("123457", "Default Sentence Type")),
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
                Order("Default Sentence Type", 12, null, startDate = LocalDate.now().minusDays(14)),
                listOf(
                    Requirement(
                        requirement1.id,
                        requirement1.mainCategory!!.code,
                        requirement1.expectedStartDate,
                        requirement1.startDate,
                        requirement1.expectedEndDate,
                        requirement1.terminationDate,
                        requirement1.terminationDetails?.description,
                        "${requirement1.mainCategory!!.description} - ${requirement1.subCategory!!.description}",
                        requirement1.length,
                        requirement1.mainCategory!!.unitDetails!!.description,
                        listOf(NoteDetail(0, note = requirement1.notes!!, hasNoteBeenTruncated = false)),
                        null
                    ),
                ),
                listOf(CourtDocument("A001", LocalDate.now(), "Pre Sentence Event")),
                "65 hours 36 minutes completed (of 70 hours)",
                listOf()
            )
        )

        assertEquals(expected, response)
        verify(eventRepository, times(1)).findSentencesByPersonId(PersonGenerator.OVERVIEW.id)
        verify(additionalSentenceRepository, times(1)).getAllByEventId(event.id)
        verify(courtAppearanceRepository, times(1)).getFirstCourtAppearanceByEventIdOrderByDate(event.id)
        verify(documentRepository, times(1)).getCourtDocuments(event.id, event.eventNumber)
        verify(requirementRepository, times(1)).sumTotalUnpaidWorkHoursByDisposal(event.disposal!!.id)
        verify(upwAppointmentRepository, times(1)).calculateUnpaidTimeWorked(event.disposal!!.id)

        verifyNoMoreInteractions(eventRepository)
        verifyNoMoreInteractions(additionalSentenceRepository)
        verifyNoMoreInteractions(courtAppearanceRepository)
        verifyNoMoreInteractions(documentRepository)
        verifyNoMoreInteractions(requirementRepository)
        verifyNoMoreInteractions(upwAppointmentRepository)
    }

    @Test
    fun `unpaid work 0 time recorded`() {

        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)

        whenever(eventRepository.findSentencesByPersonId(PersonGenerator.OVERVIEW.id)).thenReturn(listOf(event))

        whenever(requirementRepository.getRequirements(event.id, event.eventNumber))
            .thenReturn(listOf(requirement1))

        whenever(requirementRepository.sumTotalUnpaidWorkHoursByDisposal(event.disposal!!.id)).thenReturn(1)
        whenever(upwAppointmentRepository.calculateUnpaidTimeWorked(event.disposal!!.id)).thenReturn(0)

        val response = service.getEvents(PersonGenerator.OVERVIEW.crn, null)

        val expected = "0 minutes completed (of 1 hour)"


        assertEquals(expected, response.sentence!!.unpaidWorkProgress)
    }

    @Test
    fun `unpaid work one minute`() {

        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)

        whenever(eventRepository.findSentencesByPersonId(PersonGenerator.OVERVIEW.id)).thenReturn(listOf(event))

        whenever(requirementRepository.getRequirements(event.id, event.eventNumber))
            .thenReturn(listOf(requirement1))

        whenever(requirementRepository.sumTotalUnpaidWorkHoursByDisposal(event.disposal!!.id)).thenReturn(1)
        whenever(upwAppointmentRepository.calculateUnpaidTimeWorked(event.disposal!!.id)).thenReturn(1)

        val response = service.getEvents(PersonGenerator.OVERVIEW.crn, null)

        val expected = "1 minute completed (of 1 hour)"


        assertEquals(expected, response.sentence!!.unpaidWorkProgress)
    }

    @Test
    fun `unpaid work two minutes`() {

        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)

        whenever(eventRepository.findSentencesByPersonId(PersonGenerator.OVERVIEW.id)).thenReturn(listOf(event))

        whenever(requirementRepository.getRequirements(event.id, event.eventNumber))
            .thenReturn(listOf(requirement1))

        whenever(requirementRepository.sumTotalUnpaidWorkHoursByDisposal(event.disposal!!.id)).thenReturn(2)
        whenever(upwAppointmentRepository.calculateUnpaidTimeWorked(event.disposal!!.id)).thenReturn(2)

        val response = service.getEvents(PersonGenerator.OVERVIEW.crn, null)

        val expected = "2 minutes completed (of 2 hours)"

        assertEquals(expected, response.sentence!!.unpaidWorkProgress)
    }

    @Test
    fun `unpaid work 60 minutes`() {

        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)

        whenever(eventRepository.findSentencesByPersonId(PersonGenerator.OVERVIEW.id)).thenReturn(listOf(event))

        whenever(requirementRepository.getRequirements(event.id, event.eventNumber))
            .thenReturn(listOf(requirement1))

        whenever(requirementRepository.sumTotalUnpaidWorkHoursByDisposal(event.disposal!!.id)).thenReturn(1)
        whenever(upwAppointmentRepository.calculateUnpaidTimeWorked(event.disposal!!.id)).thenReturn(60)

        val response = service.getEvents(PersonGenerator.OVERVIEW.crn, null)

        val expected = Requirement(
            requirement1.id,
            requirement1.mainCategory!!.code,
            requirement1.expectedStartDate,
            requirement1.startDate,
            requirement1.expectedEndDate,
            requirement1.terminationDate,
            requirement1.terminationDetails?.description,
            "${requirement1.mainCategory!!.description} - ${requirement1.subCategory!!.description}",
            requirement1.length,
            requirement1.mainCategory!!.unitDetails!!.description,
            listOf(NoteDetail(0, note = requirement1.notes!!, hasNoteBeenTruncated = false)),
            null
        )

        assertEquals(expected, response.sentence!!.requirements[0])
    }

    @Test
    fun `unpaid work 61 minutes`() {

        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)

        whenever(eventRepository.findSentencesByPersonId(PersonGenerator.OVERVIEW.id)).thenReturn(listOf(event))

        whenever(requirementRepository.getRequirements(event.id, event.eventNumber))
            .thenReturn(listOf(requirement1))

        whenever(requirementRepository.sumTotalUnpaidWorkHoursByDisposal(event.disposal!!.id)).thenReturn(2)
        whenever(upwAppointmentRepository.calculateUnpaidTimeWorked(event.disposal!!.id)).thenReturn(61)

        val response = service.getEvents(PersonGenerator.OVERVIEW.crn, null)

        val expected = "1 hour 1 minute completed (of 2 hours)"

        assertEquals(expected, response.sentence!!.unpaidWorkProgress)
    }

    @Test
    fun `unpaid work 62 minutes`() {

        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)

        whenever(eventRepository.findSentencesByPersonId(PersonGenerator.OVERVIEW.id)).thenReturn(listOf(event))

        whenever(requirementRepository.getRequirements(event.id, event.eventNumber))
            .thenReturn(listOf(requirement1))

        whenever(requirementRepository.sumTotalUnpaidWorkHoursByDisposal(event.disposal!!.id)).thenReturn(2)
        whenever(upwAppointmentRepository.calculateUnpaidTimeWorked(event.disposal!!.id)).thenReturn(62)

        val response = service.getEvents(PersonGenerator.OVERVIEW.crn, null)

        val expected = "1 hour 2 minutes completed (of 2 hours)"

        assertEquals(expected, response.sentence!!.unpaidWorkProgress)
    }

    @Test
    fun `unpaid work 120 minutes`() {

        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)

        whenever(eventRepository.findSentencesByPersonId(PersonGenerator.OVERVIEW.id)).thenReturn(listOf(event))

        whenever(requirementRepository.getRequirements(event.id, event.eventNumber))
            .thenReturn(listOf(requirement1))

        whenever(requirementRepository.sumTotalUnpaidWorkHoursByDisposal(event.disposal!!.id)).thenReturn(1)
        whenever(upwAppointmentRepository.calculateUnpaidTimeWorked(event.disposal!!.id)).thenReturn(120)

        val response = service.getEvents(PersonGenerator.OVERVIEW.crn, null)

        val expected = Requirement(
            requirement1.id,
            requirement1.mainCategory!!.code,
            requirement1.expectedStartDate,
            requirement1.startDate,
            requirement1.expectedEndDate,
            requirement1.terminationDate,
            requirement1.terminationDetails?.description,
            "${requirement1.mainCategory!!.description} - ${requirement1.subCategory!!.description}",
            requirement1.length,
            requirement1.mainCategory!!.unitDetails!!.description,
            listOf(NoteDetail(0, note = requirement1.notes!!, hasNoteBeenTruncated = false)),
            null
        )

        assertEquals(expected, response.sentence!!.requirements[0])
    }

    @Test
    fun `unpaid work 121 minutes`() {

        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)

        whenever(eventRepository.findSentencesByPersonId(PersonGenerator.OVERVIEW.id)).thenReturn(listOf(event))

        whenever(requirementRepository.getRequirements(event.id, event.eventNumber))
            .thenReturn(listOf(requirement1))

        whenever(requirementRepository.sumTotalUnpaidWorkHoursByDisposal(event.disposal!!.id)).thenReturn(3)
        whenever(upwAppointmentRepository.calculateUnpaidTimeWorked(event.disposal!!.id)).thenReturn(121)

        val response = service.getEvents(PersonGenerator.OVERVIEW.crn, null)

        val expected = "2 hours 1 minute completed (of 3 hours)"

        assertEquals(expected, response.sentence!!.unpaidWorkProgress)
    }

    @Test
    fun `unpaid work 122 minutes`() {

        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)

        whenever(eventRepository.findSentencesByPersonId(PersonGenerator.OVERVIEW.id)).thenReturn(listOf(event))

        whenever(requirementRepository.getRequirements(event.id, event.eventNumber))
            .thenReturn(listOf(requirement1))

        whenever(requirementRepository.sumTotalUnpaidWorkHoursByDisposal(event.disposal!!.id)).thenReturn(3)
        whenever(upwAppointmentRepository.calculateUnpaidTimeWorked(event.disposal!!.id)).thenReturn(122)

        val response = service.getEvents(PersonGenerator.OVERVIEW.crn, null)

        val expected = "2 hours 2 minutes completed (of 3 hours)"

        assertEquals(expected, response.sentence!!.unpaidWorkProgress)
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