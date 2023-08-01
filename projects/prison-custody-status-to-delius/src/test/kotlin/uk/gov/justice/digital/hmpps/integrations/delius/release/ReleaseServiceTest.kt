package uk.gov.justice.digital.hmpps.integrations.delius.release

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator.DEFAULT
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.release.entity.Release
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovement
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS

internal class ReleaseServiceTest : ReleaseServiceTestBase() {

    private val person = PersonGenerator.RELEASABLE

    companion object {
        private const val RELEASED = "RELEASED"
    }

    @Test
    fun unsupportedReleaseTypeIsIgnored() {
        assertThrows<IgnorableMessageException> {
            releaseService.release(
                PrisonerMovement.Released(
                    "",
                    "",
                    "TEMPORARY_ABSENCE_RELEASE",
                    "BL",
                    ZonedDateTime.now()
                )
            )
        }
        assertThrows<IgnorableMessageException> {
            releaseService.release(
                PrisonerMovement.Released(
                    "",
                    "",
                    "RELEASED_TO_HOSPITAL",
                    "BL",
                    ZonedDateTime.now()
                )
            )
        }
        assertThrows<IgnorableMessageException> {
            releaseService.release(
                PrisonerMovement.Released(
                    "",
                    "",
                    "SENT_TO_COURT",
                    "BL",
                    ZonedDateTime.now()
                )
            )
        }
        assertThrows<IgnorableMessageException> {
            releaseService.release(
                PrisonerMovement.Released(
                    "",
                    "",
                    "TRANSFERRED",
                    "BL",
                    ZonedDateTime.now()
                )
            )
        }
    }

    @Test
    fun unexpectedReleaseTypeIsThrown() {
        assertThrows<IllegalArgumentException> {
            releaseService.release(PrisonerMovement.Released("", "", "Invalid reason!", "BL", ZonedDateTime.now()))
        }
    }

    @Test
    fun missingReleaseTypeIsThrown() {
        assertThrows<NotFoundException> {
            releaseService.release(PrisonerMovement.Released("", "", RELEASED, "BL", ZonedDateTime.now()))
        }
    }

    @Test
    fun missingInstitutionIsThrown() {
        val event = EventGenerator.custodialEvent(person, DEFAULT)

        whenever(referenceDataRepository.findByCodeAndSetName(ReleaseTypeCode.ADULT_LICENCE.code, "RELEASE TYPE"))
            .thenReturn(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.ADULT_LICENCE])
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))

        assertThrows<NotFoundException> {
            releaseService.release(
                PrisonerMovement.Released(
                    person.nomsNumber,
                    "TEST",
                    RELEASED,
                    "BL",
                    ZonedDateTime.now()
                )
            )
        }
    }

    @Test
    fun failureToRetrieveEventsIsThrown() {
        whenever(referenceDataRepository.findByCodeAndSetName(ReleaseTypeCode.ADULT_LICENCE.code, "RELEASE TYPE"))
            .thenReturn(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.ADULT_LICENCE])
        whenever(eventService.getActiveCustodialEvents("INVALID")).thenThrow(IllegalArgumentException())

        assertThrows<IllegalArgumentException> {
            releaseService.release(
                PrisonerMovement.Released(
                    "INVALID",
                    DEFAULT.code,
                    RELEASED,
                    "BL",
                    ZonedDateTime.now()
                )
            )
        }
    }

    @Test
    fun attemptToReleaseUnsentencedEventIsThrown() {
        val event = EventGenerator.unSentencedEvent(person)
        whenever(referenceDataRepository.findByCodeAndSetName(ReleaseTypeCode.ADULT_LICENCE.code, "RELEASE TYPE"))
            .thenReturn(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.ADULT_LICENCE])
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))

        val exception = assertThrows<NotFoundException> {
            releaseService.release(
                PrisonerMovement.Released(
                    person.nomsNumber,
                    DEFAULT.code,
                    RELEASED,
                    "BL",
                    ZonedDateTime.now()
                )
            )
        }
        assertThat(exception.message, matchesPattern("Disposal with eventId of \\d* not found"))
    }

    @Test
    fun attemptToReleaseNonCustodialEventIsThrown() {
        val event = EventGenerator.nonCustodialEvent(person)
        whenever(referenceDataRepository.findByCodeAndSetName(ReleaseTypeCode.ADULT_LICENCE.code, "RELEASE TYPE"))
            .thenReturn(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.ADULT_LICENCE])
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))

        val exception = assertThrows<NotFoundException> {
            releaseService.release(
                PrisonerMovement.Released(
                    person.nomsNumber,
                    DEFAULT.code,
                    RELEASED,
                    "BL",
                    ZonedDateTime.now()
                )
            )
        }
        assertThat(exception.message, matchesPattern("Custody with disposalId of \\d* not found"))
    }

    @Test
    fun unexpectedCustodialStatusIsIgnored() {
        val status = CustodialStatusCode.RELEASED_ON_LICENCE
        whenever(referenceDataRepository.findByCodeAndSetName(ReleaseTypeCode.ADULT_LICENCE.code, "RELEASE TYPE"))
            .thenReturn(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.ADULT_LICENCE])
        whenever(institutionRepository.findByNomisCdeCode(DEFAULT.code)).thenReturn(DEFAULT)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber))
            .thenReturn(listOf(EventGenerator.custodialEvent(person, DEFAULT, status)))

        val exception = assertThrows<IgnorableMessageException> {
            releaseService.release(
                PrisonerMovement.Released(
                    person.nomsNumber,
                    DEFAULT.code,
                    RELEASED,
                    "OPA",
                    ZonedDateTime.now()
                )
            )
        }
        assertEquals(exception.message, "UnexpectedCustodialStatus")
    }

    @Test
    fun unexpectedInstitutionIsIgnored() {
        val institution = InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY]!!
        whenever(referenceDataRepository.findByCodeAndSetName(ReleaseTypeCode.ADULT_LICENCE.code, "RELEASE TYPE"))
            .thenReturn(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.ADULT_LICENCE])
        whenever(institutionRepository.findByNomisCdeCode(DEFAULT.code)).thenReturn(DEFAULT)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber))
            .thenReturn(listOf(EventGenerator.custodialEvent(person, institution)))

        val exception = assertThrows<IgnorableMessageException> {
            releaseService.release(
                PrisonerMovement.Released(
                    person.nomsNumber,
                    DEFAULT.code,
                    RELEASED,
                    "OPA",
                    ZonedDateTime.now()
                )
            )
        }
        assertEquals(exception.message, "UnexpectedInstitution")
    }

    @Test
    fun releaseDateBeforeSentenceDateIsIgnored() {
        val releaseDate = LocalDate.of(2022, 1, 1).atStartOfDay(EuropeLondon)
        whenever(referenceDataRepository.findByCodeAndSetName(ReleaseTypeCode.ADULT_LICENCE.code, "RELEASE TYPE"))
            .thenReturn(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.ADULT_LICENCE])
        whenever(institutionRepository.findByNomisCdeCode(DEFAULT.code)).thenReturn(DEFAULT)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber))
            .thenReturn(listOf(EventGenerator.custodialEvent(person, DEFAULT)))

        val exception = assertThrows<IgnorableMessageException> {
            releaseService.release(
                PrisonerMovement.Released(
                    person.nomsNumber,
                    DEFAULT.code,
                    RELEASED,
                    "OPA",
                    releaseDate
                )
            )
        }
        assertThat(exception.message, equalTo("InvalidReleaseDate"))
    }

    @Test
    fun releaseDateBeforePreviousRecallDateIsIgnored() {
        val previousRecallDate = ZonedDateTime.now()
        val releaseDate = previousRecallDate.minusWeeks(1)
        val event = EventGenerator.previouslyRecalledEvent(person, DEFAULT, recallDate = previousRecallDate)
        whenever(referenceDataRepository.findByCodeAndSetName(ReleaseTypeCode.ADULT_LICENCE.code, "RELEASE TYPE"))
            .thenReturn(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.ADULT_LICENCE])
        whenever(institutionRepository.findByNomisCdeCode(DEFAULT.code)).thenReturn(DEFAULT)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))

        val exception = assertThrows<IgnorableMessageException> {
            releaseService.release(
                PrisonerMovement.Released(
                    person.nomsNumber,
                    DEFAULT.code,
                    RELEASED,
                    "OPA",
                    releaseDate
                )
            )
        }
        assertThat(exception.message, equalTo("InvalidReleaseDate"))
    }

    @Test
    fun missingOrderManagerIsThrown() {
        val event = EventGenerator.custodialEvent(person, DEFAULT)
        whenever(referenceDataRepository.findByCodeAndSetName(ReleaseTypeCode.ADULT_LICENCE.code, "RELEASE TYPE"))
            .thenReturn(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.ADULT_LICENCE])
        whenever(institutionRepository.findByNomisCdeCode(DEFAULT.code)).thenReturn(DEFAULT)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))

        val exception = assertThrows<NotFoundException> {
            releaseService.release(
                PrisonerMovement.Released(
                    person.nomsNumber,
                    DEFAULT.code,
                    RELEASED,
                    "OPA",
                    ZonedDateTime.now()
                )
            )
        }
        assertThat(exception.message, matchesPattern("OrderManager with eventId of \\d* not found"))
    }

    @Test
    fun missingContactTypeIsThrown() {
        val event = EventGenerator.custodialEvent(person, DEFAULT)
        val orderManager = OrderManagerGenerator.generate(event)
        whenever(referenceDataRepository.findByCodeAndSetName(ReleaseTypeCode.ADULT_LICENCE.code, "RELEASE TYPE"))
            .thenReturn(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.ADULT_LICENCE])
        whenever(institutionRepository.findByNomisCdeCode(DEFAULT.code)).thenReturn(DEFAULT)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(orderManager)

        val exception = assertThrows<NotFoundException> {
            releaseService.release(
                PrisonerMovement.Released(
                    person.nomsNumber,
                    DEFAULT.code,
                    RELEASED,
                    "ANY",
                    ZonedDateTime.now()
                )
            )
        }
        assertThat(exception.message, equalTo("ContactType with code of EREL not found"))
    }

    @Test
    fun successfulReleaseIsSaved() {
        val event = EventGenerator.custodialEvent(person, DEFAULT)
        val orderManager = OrderManagerGenerator.generate(event)
        val releaseDateTime = ZonedDateTime.now()
        val releaseDate = releaseDateTime.truncatedTo(DAYS)
        val inCom = InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY]!!

        whenever(institutionRepository.findByCode(inCom.code)).thenReturn(inCom)
        whenever(referenceDataRepository.findByCodeAndSetName(ReleaseTypeCode.ADULT_LICENCE.code, "RELEASE TYPE"))
            .thenReturn(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.ADULT_LICENCE])
        whenever(institutionRepository.findByNomisCdeCode(DEFAULT.nomisCdeCode!!)).thenReturn(DEFAULT)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(orderManager)
        whenever(contactTypeRepository.findByCode(ContactType.Code.RELEASE_FROM_CUSTODY.value))
            .thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactType.Code.RELEASE_FROM_CUSTODY])
        doAnswer<Contact> { it.getArgument(0) }.whenever(contactRepository).save(any())

        releaseService.release(
            PrisonerMovement.Released(
                person.nomsNumber,
                DEFAULT.nomisCdeCode,
                RELEASED,
                "OPA",
                releaseDateTime
            )
        )

        val saved = argumentCaptor<Release>()
        verify(releaseRepository).save(saved.capture())
        verify(custodyService).updateStatus(
            event.disposal!!.custody!!,
            CustodialStatusCode.RELEASED_ON_LICENCE,
            releaseDate,
            "Released on Licence"
        )
        verify(custodyService).updateLocation(
            event.disposal!!.custody!!,
            inCom,
            releaseDate
        )
        verify(eventService).updateReleaseDateAndIapsFlag(event, releaseDate)

        val release = saved.firstValue
        assertThat(release.createdDatetime, isCloseTo(ZonedDateTime.now()))
        assertThat(release.lastUpdatedDatetime, isCloseTo(ZonedDateTime.now()))
        assertThat(release.date, equalTo(releaseDate))
        assertThat(release.type, equalTo(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.ADULT_LICENCE]!!))
        assertThat(release.person, equalTo(person))
        assertThat(release.custody, equalTo(event.disposal!!.custody))
        assertThat(release.institutionId, equalTo(DEFAULT.id))
        assertThat(release.probationAreaId, equalTo(0))
    }

    @Test
    fun successfulReleaseCreatesAContact() {
        val event = EventGenerator.custodialEvent(person, DEFAULT)
        val orderManager = OrderManagerGenerator.generate(event)
        val releaseDateTime = ZonedDateTime.now()
        val inCom = InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY]!!

        whenever(referenceDataRepository.findByCodeAndSetName(ReleaseTypeCode.ADULT_LICENCE.code, "RELEASE TYPE"))
            .thenReturn(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.ADULT_LICENCE])
        whenever(institutionRepository.findByNomisCdeCode(DEFAULT.code)).thenReturn(DEFAULT)
        whenever(institutionRepository.findByCode(inCom.code)).thenReturn(inCom)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(orderManager)
        whenever(contactTypeRepository.findByCode(ContactType.Code.RELEASE_FROM_CUSTODY.value))
            .thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactType.Code.RELEASE_FROM_CUSTODY])
        doAnswer<Contact> { it.getArgument(0) }.whenever(contactRepository).save(any())

        releaseService.release(
            PrisonerMovement.Released(
                person.nomsNumber,
                DEFAULT.code,
                RELEASED,
                "OPA",
                releaseDateTime
            )
        )

        val saved = argumentCaptor<Contact>()
        verify(contactRepository).save(saved.capture())

        val contact = saved.firstValue
        assertThat(contact.createdDatetime, isCloseTo(ZonedDateTime.now()))
        assertThat(contact.lastUpdatedDatetime, isCloseTo(ZonedDateTime.now()))
        assertThat(contact.date, equalTo(releaseDateTime))
        assertThat(contact.type, equalTo(ReferenceDataGenerator.CONTACT_TYPE[ContactType.Code.RELEASE_FROM_CUSTODY]!!))
        assertThat(contact.person, equalTo(person))
        assertThat(contact.event, equalTo(event))
        assertThat(contact.notes, equalTo("Release Type: description of ADL"))
        assertThat(contact.staffId, equalTo(orderManager.staffId))
        assertThat(contact.teamId, equalTo(orderManager.teamId))
    }
}
