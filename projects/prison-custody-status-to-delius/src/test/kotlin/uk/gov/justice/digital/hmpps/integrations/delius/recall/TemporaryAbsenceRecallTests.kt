package uk.gov.justice.digital.hmpps.integrations.delius.recall

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlert
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.RecallReasonCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.TERMINATED_STATUSES
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS

class TemporaryAbsenceRecallTests : RecallServiceTestBase() {

    @Test
    fun `temporary absence with current status RoTL`() {
        val person = PersonGenerator.generate("A12345B")
        val event = EventGenerator.previouslyReleasedEvent(
            person,
            InstitutionGenerator.DEFAULT,
            custodialStatusCode = CustodialStatusCode.CUSTODY_ROTL
        )
        val recallDateTime = ZonedDateTime.now()
        val recallDate = recallDateTime.truncatedTo(DAYS)

        whenever(recallReasonRepository.findByCodeAndSelectableIsTrue(RecallReasonCode.END_OF_TEMPORARY_LICENCE.code))
            .thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.END_OF_TEMPORARY_LICENCE])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishmentIsTrue("WSI"))
            .thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))
        doAnswer<Recall> { it.getArgument(0) }.whenever(recallRepository).save(any())
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(OrderManagerGenerator.generate(event))
        whenever(personManagerRepository.findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id))
            .thenReturn(PersonManagerGenerator.generate(person))
        whenever(contactTypeRepository.findByCode(ContactTypeCode.BREACH_PRISON_RECALL.code))
            .thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.BREACH_PRISON_RECALL])
        doAnswer<Contact> { it.getArgument(0) }.whenever(contactRepository).save(any())

        recallService.recall(person.nomsNumber, "WSI", "TEMPORARY_ABSENCE_RETURN", recallDateTime)

        // recall is created
        val recall = argumentCaptor<Recall>()
        verify(recallRepository).save(recall.capture())
        assertThat(recall.firstValue.date, equalTo(recallDate))
        assertThat(recall.firstValue.createdDatetime, isCloseTo(recallDateTime))
        assertThat(recall.firstValue.lastUpdatedDatetime, isCloseTo(recallDateTime))
        assertThat(recall.firstValue.reason.code, equalTo(RecallReasonCode.END_OF_TEMPORARY_LICENCE.code))

        // custody details are updated
        verify(custodyService).updateLocation(event.disposal!!.custody!!, InstitutionGenerator.DEFAULT.code, recallDate)
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
            contact.firstValue.createdDatetime, equalTo(recall.firstValue.createdDatetime)
        )
        assertThat(contact.firstValue.event, equalTo(event))
        assertThat(contact.firstValue.date, equalTo(recallDateTime))
        assertThat(contact.firstValue.notes, equalTo("Reason for Recall: description of EOTL"))
        val contactAlert = argumentCaptor<ContactAlert>()
        verify(contactAlertRepository).save(contactAlert.capture())
        assertThat(contactAlert.firstValue.contactId, equalTo(contact.firstValue.id))
    }

    @Test
    fun `temporary absence with current status Recalled`() {
        val person = PersonGenerator.generate("A22345B")
        val event = EventGenerator.previouslyRecalledEvent(
            person,
            InstitutionGenerator.generate("TSP")
        )
        val recallDateTime = ZonedDateTime.now()
        val recallDate = recallDateTime.truncatedTo(DAYS)

        whenever(recallReasonRepository.findByCodeAndSelectableIsTrue(RecallReasonCode.END_OF_TEMPORARY_LICENCE.code))
            .thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.END_OF_TEMPORARY_LICENCE])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishmentIsTrue("WSI"))
            .thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(OrderManagerGenerator.generate(event))
        whenever(personManagerRepository.findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id))
            .thenReturn(PersonManagerGenerator.generate(person))
        whenever(contactTypeRepository.findByCode(ContactTypeCode.BREACH_PRISON_RECALL.code))
            .thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.BREACH_PRISON_RECALL])
        doAnswer<Contact> { it.getArgument(0) }.whenever(contactRepository).save(any())

        recallService.recall(person.nomsNumber, "WSI", "TEMPORARY_ABSENCE_RETURN", recallDateTime)

        // recall is not created
        verify(recallRepository, never()).save(any())

        // custody details are updated
        verify(custodyService).updateLocation(event.disposal!!.custody!!, InstitutionGenerator.DEFAULT.code, recallDate)
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
        assertThat(contact.firstValue.event, equalTo(event))
        assertThat(contact.firstValue.date, equalTo(recallDateTime))
        assertThat(contact.firstValue.notes, equalTo("Reason for Recall: description of EOTL"))
        val contactAlert = argumentCaptor<ContactAlert>()
        verify(contactAlertRepository).save(contactAlert.capture())
        assertThat(contactAlert.firstValue.contactId, equalTo(contact.firstValue.id))
    }

    @Test
    fun `temporary absence with current status In Custody`() {
        val person = PersonGenerator.generate("A32345B")
        val event = EventGenerator.custodialEvent(
            person,
            InstitutionGenerator.DEFAULT
        )
        val recallDateTime = ZonedDateTime.now()
        val recallDate = recallDateTime.truncatedTo(DAYS)

        whenever(recallReasonRepository.findByCodeAndSelectableIsTrue(RecallReasonCode.END_OF_TEMPORARY_LICENCE.code))
            .thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.END_OF_TEMPORARY_LICENCE])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishmentIsTrue("WSI"))
            .thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(OrderManagerGenerator.generate(event))
        whenever(personManagerRepository.findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id))
            .thenReturn(PersonManagerGenerator.generate(person))
        whenever(contactTypeRepository.findByCode(ContactTypeCode.BREACH_PRISON_RECALL.code))
            .thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.BREACH_PRISON_RECALL])
        doAnswer<Contact> { it.getArgument(0) }.whenever(contactRepository).save(any())

        recallService.recall(person.nomsNumber, "WSI", "TEMPORARY_ABSENCE_RETURN", recallDateTime)

        // recall is not created
        verify(recallRepository, never()).save(any())

        // custody details are not updated
        verify(custodyService, never()).updateLocation(any(), any(), any())
        verify(custodyService, never()).updateStatus(any(), any(), any(), any())

        // licence conditions are terminated
        verify(licenceConditionService).terminateLicenceConditionsForDisposal(
            event.disposal!!.id,
            ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON,
            recallDate
        )

        // contact alert is created
        val contact = argumentCaptor<Contact>()
        verify(contactRepository).save(contact.capture())
        assertThat(contact.firstValue.event, equalTo(event))
        assertThat(contact.firstValue.date, equalTo(recallDateTime))
        assertThat(contact.firstValue.notes, equalTo("Reason for Recall: description of EOTL"))
        val contactAlert = argumentCaptor<ContactAlert>()
        verify(contactAlertRepository).save(contactAlert.capture())
        assertThat(contactAlert.firstValue.contactId, equalTo(contact.firstValue.id))
    }

    @Test
    fun `temporary absence with current status In Custody - IRC`() {
        val person = PersonGenerator.generate("A42345B")
        val event = EventGenerator.custodialEvent(
            person,
            InstitutionGenerator.DEFAULT,
            custodialStatusCode = CustodialStatusCode.IN_CUSTODY_IRC
        )
        whenever(recallReasonRepository.findByCodeAndSelectableIsTrue(RecallReasonCode.END_OF_TEMPORARY_LICENCE.code))
            .thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.END_OF_TEMPORARY_LICENCE])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishmentIsTrue("WSI"))
            .thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))

        val exception = assertThrows<IgnorableMessageException> {
            recallService.recall(person.nomsNumber, "WSI", "TEMPORARY_ABSENCE_RETURN", ZonedDateTime.now())
        }
        assertThat(exception.message, equalTo("UnexpectedCustodialStatus"))
    }

    @Test
    fun `temporary absence with current status Release on Licence`() {
        val person = PersonGenerator.generate("A52345B")
        val event = EventGenerator.previouslyReleasedEvent(
            person,
            InstitutionGenerator.generate("TSP"),
        )
        val recallDateTime = ZonedDateTime.now()
        val recallDate = recallDateTime.truncatedTo(DAYS)

        whenever(recallReasonRepository.findByCodeAndSelectableIsTrue(RecallReasonCode.END_OF_TEMPORARY_LICENCE.code))
            .thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.END_OF_TEMPORARY_LICENCE])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishmentIsTrue("WSI"))
            .thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(OrderManagerGenerator.generate(event))
        whenever(personManagerRepository.findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id))
            .thenReturn(PersonManagerGenerator.generate(person))
        whenever(contactTypeRepository.findByCode(ContactTypeCode.BREACH_PRISON_RECALL.code))
            .thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.BREACH_PRISON_RECALL])
        doAnswer<Contact> { it.getArgument(0) }.whenever(contactRepository).save(any())

        recallService.recall(person.nomsNumber, "WSI", "TEMPORARY_ABSENCE_RETURN", recallDateTime)

        // recall is created
        val recall = argumentCaptor<Recall>()
        verify(recallRepository).save(recall.capture())
        assertThat(recall.firstValue.date, equalTo(recallDate))
        assertThat(recall.firstValue.createdDatetime, isCloseTo(recallDateTime))
        assertThat(recall.firstValue.lastUpdatedDatetime, isCloseTo(recallDateTime))
        assertThat(recall.firstValue.reason.code, equalTo(RecallReasonCode.END_OF_TEMPORARY_LICENCE.code))

        // custody details are updated
        verify(custodyService).updateLocation(event.disposal!!.custody!!, InstitutionGenerator.DEFAULT.code, recallDate)
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
        assertThat(contact.firstValue.event, equalTo(event))
        assertThat(contact.firstValue.date, equalTo(recallDateTime))
        assertThat(contact.firstValue.notes, equalTo("Reason for Recall: description of EOTL"))
        val contactAlert = argumentCaptor<ContactAlert>()
        verify(contactAlertRepository).save(contactAlert.capture())
        assertThat(contactAlert.firstValue.contactId, equalTo(contact.firstValue.id))
    }

    @ParameterizedTest
    @MethodSource("terminatedStatuses")
    fun `temporary absence with current status terminated throws exception`(status: CustodialStatusCode) {
        val person = PersonGenerator.generate("A62345B")
        val event = EventGenerator.custodialEvent(
            person,
            InstitutionGenerator.DEFAULT,
            custodialStatusCode = status
        )
        whenever(recallReasonRepository.findByCodeAndSelectableIsTrue(RecallReasonCode.END_OF_TEMPORARY_LICENCE.code))
            .thenReturn(ReferenceDataGenerator.RECALL_REASON[RecallReasonCode.END_OF_TEMPORARY_LICENCE])
        whenever(institutionRepository.findByNomisCdeCodeAndIdEstablishmentIsTrue("WSI"))
            .thenReturn(InstitutionGenerator.DEFAULT)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))

        val exception = assertThrows<IllegalArgumentException> {
            recallService.recall(person.nomsNumber, "WSI", "TEMPORARY_ABSENCE_RETURN", ZonedDateTime.now())
        }
        assertThat(exception.message, equalTo("TerminatedCustodialStatus"))
    }

    companion object {
        @JvmStatic
        fun terminatedStatuses() = TERMINATED_STATUSES
    }
}
