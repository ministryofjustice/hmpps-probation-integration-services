package uk.gov.justice.digital.hmpps.integrations.delius.recall

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactAlert
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.Recall
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReason
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovement
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS

internal class RecallServiceTest : RecallServiceTestBase() {
    private val person = PersonGenerator.RECALLABLE
    private val nomsNumber = person.nomsNumber
    private val prisonId = InstitutionGenerator.DEFAULT.nomisCdeCode!!
    private val reason = "ADMISSION"
    private val recallDateTime = ZonedDateTime.now()
    private val recallDate = recallDateTime.truncatedTo(DAYS)

    @Test
    fun unsupportedReleaseTypeIsIgnored() {
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))
        assertThrows<IgnorableMessageException> {
            recallService.recall(
                PrisonerMovement.Received(
                    nomsNumber,
                    prisonId,
                    "RETURN_FROM_COURT",
                    "R1",
                    recallDateTime
                )
            )
        }
        assertThrows<IgnorableMessageException> {
            recallService.recall(
                PrisonerMovement.Received(
                    nomsNumber,
                    prisonId,
                    "UNKNOWN",
                    "UNKNOWN",
                    recallDateTime
                )
            )
        }
        assertThrows<IgnorableMessageException> {
            recallService.recall(
                PrisonerMovement.Received(
                    nomsNumber,
                    prisonId,
                    "TRANSFERRED",
                    "NOTINT",
                    recallDateTime
                )
            )
        }
    }

    @Test
    fun `transfer messages are processed`() {
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        val om = OrderManagerGenerator.generate(event)
        val recallReason = ReferenceDataGenerator.RECALL_REASON[RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT]

        whenever(recallReasonRepository.findByCode(recallReason!!.code)).thenReturn(recallReason)
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishment(prisonId)).thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))
        doAnswer<Recall> { it.getArgument(0) }.whenever(recallRepository).save(any())
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(om)
        whenever(personManagerRepository.findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id))
            .thenReturn(PersonManagerGenerator.generate(person))
        whenever(contactTypeRepository.findByCode(ContactType.Code.BREACH_PRISON_RECALL.value))
            .thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactType.Code.BREACH_PRISON_RECALL])
        doAnswer<Contact> { it.getArgument(0) }.whenever(contactRepository).save(any())

        val outcome = recallService.recall(PrisonerMovement.Received(nomsNumber, prisonId, "TRANSFERRED", "INT", recallDateTime))
        assertThat(outcome, equalTo(RecallOutcome.PrisonerRecalled))

        // recall is created
        val recall = argumentCaptor<Recall>()
        verify(recallRepository).save(recall.capture())
        assertThat(recall.firstValue.date, equalTo(recallDate))
        assertThat(recall.firstValue.createdDatetime, isCloseTo(ZonedDateTime.now()))
        assertThat(recall.firstValue.lastUpdatedDatetime, isCloseTo(ZonedDateTime.now()))
        assertThat(recall.firstValue.reason.code, equalTo("NN"))

        // custody details are updated
        verify(custodyService).updateLocation(
            event.disposal!!.custody!!,
            InstitutionGenerator.DEFAULT,
            recallDate,
            om,
            recallReason
        )
        verify(custodyService).updateStatus(
            event.disposal!!.custody!!,
            CustodialStatusCode.IN_CUSTODY,
            recallDate,
            "Recall added in custody "
        )

        // licence conditions are terminated
        verify(licenceConditionService).terminateLicenceConditionsForDisposal(
            event.disposal!!.id,
            ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON,
            recallDate
        )

        // contact alert is created
        val contact = argumentCaptor<Contact>()
        verify(contactRepository).save(contact.capture())
        assertThat(
            "Contact and recall must have the same created datetime, so that Delius can link them",
            contact.firstValue.createdDatetime,
            equalTo(recall.firstValue.createdDatetime)
        )
        assertThat(contact.firstValue.event, equalTo(event))
        assertThat(contact.firstValue.date, equalTo(recallDateTime))
        assertThat(contact.firstValue.notes, equalTo("Reason for Recall: description of NN"))
        val contactAlert = argumentCaptor<ContactAlert>()
        verify(contactAlertRepository).save(contactAlert.capture())
        assertThat(contactAlert.firstValue.contactId, equalTo(contact.firstValue.id))
    }

    @Test
    fun unexpectedReleaseTypeIsThrown() {
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))
        assertThrows<IllegalArgumentException> {
            recallService.recall(PrisonerMovement.Received(nomsNumber, prisonId, "Invalid reason!", "NO", recallDateTime))
        }
    }

    @Test
    fun missingReleaseTypeIsThrown() {
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))
        val ex = assertThrows<NotFoundException> {
            recallService.recall(PrisonerMovement.Received(person.nomsNumber, "", reason, "INT", recallDateTime))
        }
        assertThat(ex.message, equalTo("RecallReason with code of NN not found"))
    }

    @Test
    fun missingInstitutionIsThrown() {
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        val recallReason = ReferenceDataGenerator.RECALL_REASON[RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT]
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))
        whenever(recallReasonRepository.findByCode(recallReason!!.code))
            .thenReturn(recallReason)
        doAnswer<Recall> { it.getArgument(0) }.whenever(recallRepository).save(any())
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(OrderManagerGenerator.generate(event))
        whenever(personManagerRepository.findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id)).thenReturn(
            PersonManagerGenerator.generate(person)
        )
        whenever(contactTypeRepository.findByCode(ContactType.Code.BREACH_PRISON_RECALL.value)).thenReturn(
            ReferenceDataGenerator.CONTACT_TYPE[ContactType.Code.BREACH_PRISON_RECALL]
        )
        doAnswer<Contact> { it.getArgument(0) }.whenever(contactRepository).save(any())

        val ex = assertThrows<NotFoundException> {
            recallService.recall(PrisonerMovement.Received(person.nomsNumber, "TEST", reason, "INT", recallDateTime))
        }
        assertThat(ex.message, equalTo("Institution with nomisCdeCode of TEST not found"))
    }

    @Test
    fun failureToRetrieveEventsIsThrown() {
        whenever(eventService.getActiveCustodialEvents("INVALID")).thenThrow(IllegalArgumentException())

        assertThrows<IllegalArgumentException> {
            recallService.recall(PrisonerMovement.Received("INVALID", prisonId, reason, "INT", recallDateTime))
        }
    }

    @Test
    fun attemptToRecallUnSentencedEventIsThrown() {
        val unSentencedEvent = EventGenerator.unSentencedEvent(person)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(unSentencedEvent))

        val exception = assertThrows<NotFoundException> {
            recallService.recall(PrisonerMovement.Received(nomsNumber, prisonId, reason, "INT", recallDateTime))
        }
        assertThat(exception.message, equalTo("Disposal with eventId of ${unSentencedEvent.id} not found"))
    }

    @Test
    fun attemptToRecallNonCustodialEventIsThrown() {
        val nonCustodialEvent = EventGenerator.nonCustodialEvent(person)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(nonCustodialEvent))

        val exception = assertThrows<NotFoundException> {
            recallService.recall(PrisonerMovement.Received(nomsNumber, prisonId, reason, "INT", recallDateTime))
        }
        assertThat(
            exception.message,
            equalTo("Custody with disposalId of ${nonCustodialEvent.disposal!!.id} not found")
        )
    }

    @Test
    fun unexpectedCustodialStatusIsIgnored() {
        val status = CustodialStatusCode.POST_SENTENCE_SUPERVISION
        val event =
            EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT, custodialStatusCode = status)
        whenever(recallReasonRepository.findByCode(RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.value)).thenReturn(
            ReferenceDataGenerator.RECALL_REASON[RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT]
        )
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))

        val exception = assertThrows<IgnorableMessageException> {
            recallService.recall(PrisonerMovement.Received(nomsNumber, prisonId, reason, "INT", recallDateTime))
        }
        assertThat(exception.message, equalTo("UnexpectedCustodialStatus"))
    }

    @Test
    fun futureRecallDateIsIgnored() {
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        whenever(recallReasonRepository.findByCode(RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.value)).thenReturn(
            ReferenceDataGenerator.RECALL_REASON[RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT]
        )
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))

        val exception = assertThrows<IgnorableMessageException> {
            recallService.recall(PrisonerMovement.Received(nomsNumber, prisonId, reason, "INT", recallDateTime.plusDays(1)))
        }
        assertThat(exception.message, equalTo("InvalidRecallDate"))
    }

    @Test
    fun recallDateBeforePreviousReleaseDateIsIgnored() {
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        whenever(recallReasonRepository.findByCode(RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.value)).thenReturn(
            ReferenceDataGenerator.RECALL_REASON[RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT]
        )
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))

        val exception = assertThrows<IgnorableMessageException> {
            recallService.recall(PrisonerMovement.Received(nomsNumber, prisonId, reason, "INT", event.firstReleaseDate!!.minusDays(1)))
        }
        assertThat(exception.message, equalTo("InvalidRecallDate"))
    }

    @Test
    fun missingOrderManagerIsThrown() {
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        whenever(recallReasonRepository.findByCode(RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.value)).thenReturn(
            ReferenceDataGenerator.RECALL_REASON[RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT]
        )

        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))
        doAnswer<Recall> { it.getArgument(0) }.whenever(recallRepository).save(any())

        val exception = assertThrows<NotFoundException> {
            recallService.recall(PrisonerMovement.Received(nomsNumber, prisonId, reason, "INT", recallDateTime))
        }
        assertThat(exception.message, equalTo("OrderManager with eventId of ${event.id} not found"))
    }

    @Test
    fun missingPersonManagerIsThrown() {
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        whenever(recallReasonRepository.findByCode(RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.value)).thenReturn(
            ReferenceDataGenerator.RECALL_REASON[RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT]
        )

        whenever(contactTypeRepository.findByCode(ContactType.Code.BREACH_PRISON_RECALL.value))
            .thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactType.Code.BREACH_PRISON_RECALL])
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))
        doAnswer<Recall> { it.getArgument(0) }.whenever(recallRepository).save(any())
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(OrderManagerGenerator.generate(event))
        doAnswer<Contact> { it.getArgument(0) }.whenever(contactRepository).save(any())

        val exception = assertThrows<NotFoundException> {
            recallService.recall(PrisonerMovement.Received(nomsNumber, prisonId, reason, "INT", recallDateTime))
        }
        assertThat(exception.message, equalTo("PersonManager with personId of ${person.id} not found"))
    }

    @Test
    fun missingContactTypeIsThrown() {
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        whenever(recallReasonRepository.findByCode(RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.value)).thenReturn(
            ReferenceDataGenerator.RECALL_REASON[RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT]
        )

        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))
        doAnswer<Recall> { it.getArgument(0) }.whenever(recallRepository).save(any())
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(OrderManagerGenerator.generate(event))

        val exception = assertThrows<NotFoundException> {
            recallService.recall(PrisonerMovement.Received(nomsNumber, prisonId, reason, "INT", recallDateTime))
        }
        assertThat(exception.message, equalTo("ContactType with code of ERCL not found"))
    }

    @Test
    fun recallIsSaved() {
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        val om = OrderManagerGenerator.generate(event)
        val recallReason = ReferenceDataGenerator.RECALL_REASON[RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT]
        whenever(recallReasonRepository.findByCode(recallReason!!.code))
            .thenReturn(recallReason)
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishment(prisonId)).thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))
        doAnswer<Recall> { it.getArgument(0) }.whenever(recallRepository).save(any())
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(om)
        whenever(personManagerRepository.findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id))
            .thenReturn(PersonManagerGenerator.generate(person))
        whenever(contactTypeRepository.findByCode(ContactType.Code.BREACH_PRISON_RECALL.value))
            .thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactType.Code.BREACH_PRISON_RECALL])
        doAnswer<Contact> { it.getArgument(0) }.whenever(contactRepository).save(any())

        val outcome = recallService.recall(PrisonerMovement.Received(nomsNumber, prisonId, reason, "INT", recallDateTime))
        assertThat(outcome, equalTo(RecallOutcome.PrisonerRecalled))

        // recall is created
        val recall = argumentCaptor<Recall>()
        verify(recallRepository).save(recall.capture())
        assertThat(recall.firstValue.date, equalTo(recallDate))
        assertThat(recall.firstValue.createdDatetime, isCloseTo(ZonedDateTime.now()))
        assertThat(recall.firstValue.lastUpdatedDatetime, isCloseTo(ZonedDateTime.now()))
        assertThat(recall.firstValue.reason.code, equalTo("NN"))

        // custody details are updated
        verify(custodyService).updateLocation(
            event.disposal!!.custody!!,
            InstitutionGenerator.DEFAULT,
            recallDate,
            om,
            recallReason
        )
        verify(custodyService).updateStatus(
            event.disposal!!.custody!!,
            CustodialStatusCode.IN_CUSTODY,
            recallDate,
            "Recall added in custody "
        )

        // licence conditions are terminated
        verify(licenceConditionService).terminateLicenceConditionsForDisposal(
            event.disposal!!.id,
            ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON,
            recallDate
        )

        // contact alert is created
        val contact = argumentCaptor<Contact>()
        verify(contactRepository).save(contact.capture())
        assertThat(
            "Contact and recall must have the same created datetime, so that Delius can link them",
            contact.firstValue.createdDatetime,
            equalTo(recall.firstValue.createdDatetime)
        )
        assertThat(contact.firstValue.event, equalTo(event))
        assertThat(contact.firstValue.date, equalTo(recallDateTime))
        assertThat(contact.firstValue.notes, equalTo("Reason for Recall: description of NN"))
        val contactAlert = argumentCaptor<ContactAlert>()
        verify(contactAlertRepository).save(contactAlert.capture())
        assertThat(contactAlert.firstValue.contactId, equalTo(contact.firstValue.id))
    }

    @ParameterizedTest
    @MethodSource("recallOutcomes")
    fun `correct outcome is applied across events`(input: List<RecallOutcome>, output: RecallOutcome) {
        assertThat(input.combined(), equalTo(output))
    }

    companion object {
        @JvmStatic
        fun recallOutcomes() = listOf(
            Arguments.of(listOf(RecallOutcome.PrisonerRecalled), RecallOutcome.PrisonerRecalled),
            Arguments.of(
                listOf(
                    RecallOutcome.PrisonerRecalled,
                    RecallOutcome.PrisonerRecalled,
                    RecallOutcome.CustodialDetailsUpdated
                ),
                RecallOutcome.MultipleEventsRecalled
            ),
            Arguments.of(
                listOf(
                    RecallOutcome.CustodialDetailsUpdated,
                    RecallOutcome.CustodialDetailsUpdated,
                    RecallOutcome.NoCustodialUpdates
                ),
                RecallOutcome.MultipleDetailsUpdated
            ),
            Arguments.of(
                listOf(RecallOutcome.NoCustodialUpdates, RecallOutcome.NoCustodialUpdates),
                RecallOutcome.NoCustodialUpdates
            )
        )
    }
}
