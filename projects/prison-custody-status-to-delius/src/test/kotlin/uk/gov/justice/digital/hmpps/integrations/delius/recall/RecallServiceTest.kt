package uk.gov.justice.digital.hmpps.integrations.delius.recall

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlert
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.RecallReasonCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS

internal class RecallServiceTest : RecallServiceTestBase() {
    private val person = PersonGenerator.RECALLABLE
    private val nomsNumber = person.nomsNumber
    private val prisonId = InstitutionGenerator.DEFAULT.code
    private val reason = "ADMISSION"
    private val recallDateTime = ZonedDateTime.now()
    private val recallDate = recallDateTime.truncatedTo(DAYS)

    @Test
    fun unsupportedReleaseTypeIsIgnored() {
        assertThrows<IgnorableMessageException> { recallService.recall("", "", "RETURN_FROM_COURT", recallDateTime) }
        assertThrows<IgnorableMessageException> { recallService.recall("", "", "TRANSFERRED", recallDateTime) }
        assertThrows<IgnorableMessageException> { recallService.recall("", "", "UNKNOWN", recallDateTime) }
    }

    @Test
    fun unexpectedReleaseTypeIsThrown() {
        assertThrows<IllegalArgumentException> {
            recallService.recall("", "", "Invalid reason!", recallDateTime)
        }
    }

    @Test
    fun missingReleaseTypeIsThrown() {
        assertThrows<NotFoundException> {
            recallService.recall("", "", reason, recallDateTime)
        }
    }

    @Test
    fun missingInstitutionIsThrown() {
        whenever(recallReasonRepository.findByCodeAndSelectable(RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.code)).thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT])

        assertThrows<NotFoundException> {
            recallService.recall("", "TEST", reason, recallDateTime)
        }
    }

    @Test
    fun failureToRetrieveEventsIsThrown() {
        whenever(recallReasonRepository.findByCodeAndSelectable(RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.code)).thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishment(prisonId)).thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents("INVALID")).thenThrow(IllegalArgumentException())

        assertThrows<IllegalArgumentException> {
            recallService.recall("INVALID", prisonId, reason, recallDateTime)
        }
    }

    @Test
    fun attemptToRecallUnSentencedEventIsThrown() {
        val unSentencedEvent = EventGenerator.unSentencedEvent(person)
        whenever(recallReasonRepository.findByCodeAndSelectable(RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.code)).thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishment(prisonId)).thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(unSentencedEvent))

        val exception = assertThrows<NotFoundException> {
            recallService.recall(nomsNumber, prisonId, reason, recallDateTime)
        }
        assertThat(exception.message, equalTo("Disposal with eventId of ${unSentencedEvent.id} not found"))
    }

    @Test
    fun attemptToRecallNonCustodialEventIsThrown() {
        val nonCustodialEvent = EventGenerator.nonCustodialEvent(person)
        whenever(recallReasonRepository.findByCodeAndSelectable(RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.code)).thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishment(prisonId)).thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(nonCustodialEvent))

        val exception = assertThrows<NotFoundException> {
            recallService.recall(nomsNumber, prisonId, reason, recallDateTime)
        }
        assertThat(exception.message, equalTo("Custody with disposalId of ${nonCustodialEvent.disposal!!.id} not found"))
    }

    @Test
    fun missingReleaseIsIgnored() {
        val event = EventGenerator.custodialEvent(person, InstitutionGenerator.DEFAULT)
        whenever(recallReasonRepository.findByCodeAndSelectable(RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.code)).thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishment(prisonId)).thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))

        assertThrows<IgnorableMessageException> {
            recallService.recall(nomsNumber, prisonId, reason, recallDateTime)
        }
    }

    @Test
    fun unexpectedCustodialStatusIsIgnored() {
        val status = CustodialStatusCode.POST_SENTENCE_SUPERVISION
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT, custodialStatusCode = status)
        whenever(recallReasonRepository.findByCodeAndSelectable(RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.code)).thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishment(prisonId)).thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))

        val exception = assertThrows<IgnorableMessageException> {
            recallService.recall(nomsNumber, prisonId, reason, recallDateTime)
        }
        assertThat(exception.message, equalTo("UnexpectedCustodialStatus"))
    }

    @Test
    fun recallAlreadyExistsIsIgnored() {
        val event = EventGenerator.previouslyRecalledEvent(person, InstitutionGenerator.DEFAULT)
        whenever(recallReasonRepository.findByCodeAndSelectable(RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.code)).thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishment(prisonId)).thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))

        val exception = assertThrows<IgnorableMessageException> {
            recallService.recall(nomsNumber, prisonId, reason, recallDateTime)
        }
        assertThat(exception.message, equalTo("RecallAlreadyExists"))
    }

    @Test
    fun unexpectedReleaseTypeIsIgnored() {
        val releaseType = ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT, releaseType = releaseType)
        whenever(recallReasonRepository.findByCodeAndSelectable(RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.code)).thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishment(prisonId)).thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))

        val exception = assertThrows<IgnorableMessageException> {
            recallService.recall(nomsNumber, prisonId, reason, recallDateTime)
        }
        assertThat(exception.message, equalTo("UnexpectedReleaseType"))
    }

    @Test
    fun futureRecallDateIsIgnored() {
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        whenever(recallReasonRepository.findByCodeAndSelectable(RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.code)).thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishment(prisonId)).thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))

        val exception = assertThrows<IgnorableMessageException> {
            recallService.recall(nomsNumber, prisonId, reason, recallDateTime.plusDays(1))
        }
        assertThat(exception.message, equalTo("InvalidRecallDate"))
    }

    @Test
    fun recallDateBeforePreviousReleaseDateIsIgnored() {
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        whenever(recallReasonRepository.findByCodeAndSelectable(RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.code)).thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishment(prisonId)).thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))

        val exception = assertThrows<IgnorableMessageException> {
            recallService.recall(nomsNumber, prisonId, reason, event.firstReleaseDate!!.minusDays(1))
        }
        assertThat(exception.message, equalTo("InvalidRecallDate"))
    }

    @Test
    fun missingOrderManagerIsThrown() {
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        whenever(recallReasonRepository.findByCodeAndSelectable(RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.code)).thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishment(prisonId)).thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))
        doAnswer<Recall> { it.getArgument(0) }.whenever(recallRepository).save(any())

        val exception = assertThrows<NotFoundException> {
            recallService.recall(nomsNumber, prisonId, reason, recallDateTime)
        }
        assertThat(exception.message, equalTo("OrderManager with eventId of ${event.id} not found"))
    }

    @Test
    fun missingPersonManagerIsThrown() {
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        whenever(recallReasonRepository.findByCodeAndSelectable(RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.code)).thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishment(prisonId)).thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))
        doAnswer<Recall> { it.getArgument(0) }.whenever(recallRepository).save(any())
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(OrderManagerGenerator.generate(event))

        val exception = assertThrows<NotFoundException> {
            recallService.recall(nomsNumber, prisonId, reason, recallDateTime)
        }
        assertThat(exception.message, equalTo("PersonManager with personId of ${person.id} not found"))
    }

    @Test
    fun missingContactTypeIsThrown() {
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        whenever(recallReasonRepository.findByCodeAndSelectable(RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.code)).thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishment(prisonId)).thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))
        doAnswer<Recall> { it.getArgument(0) }.whenever(recallRepository).save(any())
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(OrderManagerGenerator.generate(event))
        whenever(personManagerRepository.findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id)).thenReturn(PersonManagerGenerator.generate(person))

        val exception = assertThrows<NotFoundException> {
            recallService.recall(nomsNumber, prisonId, reason, recallDateTime)
        }
        assertThat(exception.message, equalTo("ContactType with code of ERCL not found"))
    }

    @Test
    fun recallIsSaved() {
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        val om = OrderManagerGenerator.generate(event)
        val recallReason = ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT]
        whenever(recallReasonRepository.findByCodeAndSelectable(recallReason!!.code))
            .thenReturn(recallReason)
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishment(prisonId)).thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))
        doAnswer<Recall> { it.getArgument(0) }.whenever(recallRepository).save(any())
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(om)
        whenever(personManagerRepository.findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id))
            .thenReturn(PersonManagerGenerator.generate(person))
        whenever(contactTypeRepository.findByCode(ContactTypeCode.BREACH_PRISON_RECALL.code))
            .thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.BREACH_PRISON_RECALL])
        doAnswer<Contact> { it.getArgument(0) }.whenever(contactRepository).save(any())

        val outcome = recallService.recall(nomsNumber, prisonId, reason, recallDateTime)
        assertThat(outcome, equalTo(RecallOutcome.PrisonerRecalled))

        // recall is created
        val recall = argumentCaptor<Recall>()
        verify(recallRepository).save(recall.capture())
        assertThat(recall.firstValue.date, equalTo(recallDate))
        assertThat(recall.firstValue.createdDatetime, isCloseTo(ZonedDateTime.now()))
        assertThat(recall.firstValue.lastUpdatedDatetime, isCloseTo(ZonedDateTime.now()))
        assertThat(recall.firstValue.reason.code, equalTo("NN"))

        // custody details are updated
        verify(custodyService).updateLocation(event.disposal!!.custody!!, InstitutionGenerator.DEFAULT.code, recallDate, om, recallReason)
        verify(custodyService).updateStatus(event.disposal!!.custody!!, CustodialStatusCode.IN_CUSTODY, recallDate, "Recall added in custody ")

        // licence conditions are terminated
        verify(licenceConditionService).terminateLicenceConditionsForDisposal(event.disposal!!.id, ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON, recallDate)

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
    fun recallToUnlawfullyAtLargeSetsCustodyStatusToRecalled() {
        val institution = InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.UNLAWFULLY_AT_LARGE]!!
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        whenever(recallReasonRepository.findByCodeAndSelectable(RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.code)).thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishment(institution.code)).thenReturn(institution)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))
        doAnswer<Recall> { it.getArgument(0) }.whenever(recallRepository).save(any())
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(OrderManagerGenerator.generate(event))
        whenever(personManagerRepository.findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id)).thenReturn(PersonManagerGenerator.generate(person))
        whenever(contactTypeRepository.findByCode(ContactTypeCode.BREACH_PRISON_RECALL.code)).thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.BREACH_PRISON_RECALL])
        doAnswer<Contact> { it.getArgument(0) }.whenever(contactRepository).save(any())

        recallService.recall(nomsNumber, institution.code, reason, recallDateTime)

        verify(custodyService)
            .updateStatus(event.disposal!!.custody!!, CustodialStatusCode.RECALLED, recallDate, "Recall added unlawfully at large ")
    }

    @Test
    fun recallToUnknownSetsCustodyStatusToRecalled() {
        val institution = InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.UNKNOWN]!!
        val event = EventGenerator.previouslyReleasedEvent(person, InstitutionGenerator.DEFAULT)
        whenever(recallReasonRepository.findByCodeAndSelectable(RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT.code)).thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishment(institution.code)).thenReturn(institution)
        whenever(eventService.getActiveCustodialEvents(nomsNumber)).thenReturn(listOf(event))
        doAnswer<Recall> { it.getArgument(0) }.whenever(recallRepository).save(any())
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(OrderManagerGenerator.generate(event))
        whenever(personManagerRepository.findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id)).thenReturn(PersonManagerGenerator.generate(person))
        whenever(contactTypeRepository.findByCode(ContactTypeCode.BREACH_PRISON_RECALL.code)).thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.BREACH_PRISON_RECALL])
        doAnswer<Contact> { it.getArgument(0) }.whenever(contactRepository).save(any())

        recallService.recall(nomsNumber, institution.code, reason, recallDateTime)

        verify(custodyService)
            .updateStatus(event.disposal!!.custody!!, CustodialStatusCode.RECALLED, recallDate, "Recall added but location unknown ")
    }
}
