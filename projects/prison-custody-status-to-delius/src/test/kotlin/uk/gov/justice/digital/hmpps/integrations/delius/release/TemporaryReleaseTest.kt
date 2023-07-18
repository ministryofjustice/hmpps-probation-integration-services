package uk.gov.justice.digital.hmpps.integrations.delius.release

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.FeatureFlagCodes
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.custodialEvent
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.custody.keydate.entity.KeyDate
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@ExtendWith(MockitoExtension::class)
class TemporaryReleaseTest : ReleaseServiceTestBase() {

    @Test
    fun `when feature flag is disabled, ETL23 is ignored`() {
        whenever(featureFlags.enabled(FeatureFlagCodes.RELEASE_ETL23)).thenReturn(false)
        assertThrows<IgnorableMessageException> {
            releaseService.release(
                "",
                "",
                "TEMPORARY_ABSENCE_RELEASE",
                "ETL23",
                ZonedDateTime.now()
            )
        }
    }

    @Test
    fun `exception thrown when no Auto Conditional Release date`() {
        val person = PersonGenerator.RELEASABLE
        val event = custodialEvent(person, InstitutionGenerator.DEFAULT)
        val releaseDateTime = ZonedDateTime.now()
        val institution = InstitutionGenerator.DEFAULT

        whenever(featureFlags.enabled(FeatureFlagCodes.RELEASE_ETL23)).thenReturn(true)
        whenever(
            referenceDataRepository.findByCodeAndSetName(
                ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE.code,
                "RELEASE TYPE"
            )
        ).thenReturn(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE])
        whenever(institutionRepository.findByNomisCdeCode(InstitutionGenerator.DEFAULT.nomisCdeCode!!)).thenReturn(
            institution
        )
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))

        val exception = assertThrows<IgnorableMessageException> {
            releaseService.release(
                person.nomsNumber,
                institution.nomisCdeCode,
                "TEMPORARY_ABSENCE_RELEASE",
                "ETL23",
                releaseDateTime
            )
        }

        assertThat(exception.message, equalTo("No Auto-Conditional Release date is present"))

        verify(releaseRepository, never()).save(any())
        verify(custodyService, never()).updateStatus(any(), any(), any(), any())
        verify(custodyService, never()).updateLocation(any(), any(), any(), any(), any())
        verify(eventService, never()).updateReleaseDateAndIapsFlag(any(), any())
    }

    @Test
    fun `exception thrown if acr date in past`() {
        val person = PersonGenerator.RELEASABLE
        val event = custodialEvent(person, InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.UNKNOWN]!!)
        val releaseDateTime = ZonedDateTime.now()
        val institution = InstitutionGenerator.DEFAULT
        val custody = event.disposal!!.custody!!
        val acrKd = KeyDate(custody.id, ReferenceDataGenerator.ACR_DATE_TYPE, LocalDate.now().minusDays(1))

        whenever(featureFlags.enabled(FeatureFlagCodes.RELEASE_ETL23)).thenReturn(true)
        whenever(
            referenceDataRepository.findByCodeAndSetName(
                ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE.code,
                "RELEASE TYPE"
            )
        ).thenReturn(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE])
        whenever(institutionRepository.findByNomisCdeCode(InstitutionGenerator.DEFAULT.nomisCdeCode!!)).thenReturn(
            institution
        )
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))
        whenever(custodyService.findAutoConditionalReleaseDate(custody.id)).thenReturn(acrKd)

        val exception = assertThrows<IgnorableMessageException> {
            releaseService.release(
                person.nomsNumber,
                institution.nomisCdeCode,
                "TEMPORARY_ABSENCE_RELEASE",
                "ETL23",
                releaseDateTime
            )
        }

        assertThat(exception.message, equalTo("Auto-Conditional Release date in the past"))

        verify(releaseRepository, never()).save(any())
        verify(custodyService, never()).updateStatus(any(), any(), any(), any())
        verify(custodyService, never()).updateLocation(any(), any(), any(), any(), any())
        verify(eventService, never()).updateReleaseDateAndIapsFlag(any(), any())
    }

    @Test
    fun `temporary release from custody`() {
        val person = PersonGenerator.RELEASABLE
        val event =
            custodialEvent(person, InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.UNKNOWN]!!)
        val orderManager = OrderManagerGenerator.generate(event)
        val releaseDateTime = ZonedDateTime.now()
        val releaseDate = releaseDateTime.truncatedTo(ChronoUnit.DAYS)
        val institution = InstitutionGenerator.DEFAULT
        val custody = event.disposal!!.custody!!
        val acrKd = KeyDate(custody.id, ReferenceDataGenerator.ACR_DATE_TYPE, LocalDate.now().plusDays(7))

        whenever(featureFlags.enabled(FeatureFlagCodes.RELEASE_ETL23)).thenReturn(true)
        whenever(
            referenceDataRepository.findByCodeAndSetName(
                ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE.code,
                "RELEASE TYPE"
            )
        ).thenReturn(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE])
        whenever(institutionRepository.findByNomisCdeCode(InstitutionGenerator.DEFAULT.nomisCdeCode!!)).thenReturn(
            institution
        )
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(orderManager)
        whenever(contactTypeRepository.findByCode(ContactTypeCode.RELEASE_FROM_CUSTODY.code))
            .thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.RELEASE_FROM_CUSTODY])
        whenever(custodyService.findAutoConditionalReleaseDate(custody.id)).thenReturn(acrKd)

        releaseService.release(
            person.nomsNumber,
            institution.nomisCdeCode,
            "TEMPORARY_ABSENCE_RELEASE",
            "ETL23",
            releaseDateTime
        )

        val saved = argumentCaptor<Release>()
        verify(releaseRepository).save(saved.capture())
        verify(eventService).updateReleaseDateAndIapsFlag(event, releaseDate)

        val release = saved.firstValue
        assertThat(release.date, equalTo(releaseDate))
        assertThat(
            release.type,
            equalTo(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE]!!)
        )
        assertThat(release.person, equalTo(person))
        assertThat(release.custody, equalTo(custody))
        assertThat(release.institutionId, equalTo(InstitutionGenerator.DEFAULT.id))
        assertThat(release.probationAreaId, equalTo(0))

        verify(custodyService).addRotlEndDate(acrKd)

        val savedContact = argumentCaptor<Contact>()
        verify(contactRepository).save(savedContact.capture())

        val contact = savedContact.firstValue
        assertThat(contact.type.code, equalTo(ContactTypeCode.RELEASE_FROM_CUSTODY.code))
        assertThat(contact.notes, containsString("This is a ROTL release on Extended Temporary Licence (ETL23)"))

        verify(custodyService).updateStatus(
            custody,
            CustodialStatusCode.CUSTODY_ROTL,
            releaseDate,
            "Released on Temporary Licence"
        )
        verify(custodyService).updateLocation(custody, institution, releaseDate)
    }

    @Test
    fun `temporary release when released`() {
        val person = PersonGenerator.RECALLABLE
        val event = custodialEvent(
            person,
            InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY]!!,
            CustodialStatusCode.RELEASED_ON_LICENCE
        )
        val releaseDateTime = ZonedDateTime.now()
        val releaseDate = releaseDateTime.truncatedTo(ChronoUnit.DAYS)
        val institution = InstitutionGenerator.DEFAULT
        val custody = event.disposal!!.custody!!

        whenever(featureFlags.enabled(FeatureFlagCodes.RELEASE_ETL23)).thenReturn(true)
        whenever(
            referenceDataRepository.findByCodeAndSetName(
                ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE.code,
                "RELEASE TYPE"
            )
        ).thenReturn(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE])
        whenever(institutionRepository.findByNomisCdeCode(InstitutionGenerator.DEFAULT.nomisCdeCode!!)).thenReturn(
            institution
        )
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))

        releaseService.release(
            person.nomsNumber,
            institution.nomisCdeCode,
            "TEMPORARY_ABSENCE_RELEASE",
            "ETL23",
            releaseDateTime
        )

        verify(releaseRepository, never()).save(any())
        verify(eventService, never()).updateReleaseDateAndIapsFlag(any(), any())
        verify(custodyService, never()).addRotlEndDate(any())
        verify(contactRepository, never()).save(any())

        verify(custodyService).updateStatus(
            custody,
            CustodialStatusCode.CUSTODY_ROTL,
            releaseDate,
            "Released on Temporary Licence"
        )
        verify(custodyService).updateLocation(custody, institution, releaseDate)
    }

    @Test
    fun `temporary release when RoTL`() {
        val person = PersonGenerator.RECALLABLE
        val event = custodialEvent(
            person,
            InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY]!!,
            CustodialStatusCode.CUSTODY_ROTL
        )
        val releaseDateTime = ZonedDateTime.now()
        val releaseDate = releaseDateTime.truncatedTo(ChronoUnit.DAYS)
        val institution = InstitutionGenerator.DEFAULT
        val custody = event.disposal!!.custody!!

        whenever(featureFlags.enabled(FeatureFlagCodes.RELEASE_ETL23)).thenReturn(true)
        whenever(
            referenceDataRepository.findByCodeAndSetName(
                ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE.code,
                "RELEASE TYPE"
            )
        ).thenReturn(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE])
        whenever(institutionRepository.findByNomisCdeCode(InstitutionGenerator.DEFAULT.nomisCdeCode!!)).thenReturn(
            institution
        )
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))

        releaseService.release(
            person.nomsNumber,
            institution.nomisCdeCode,
            "TEMPORARY_ABSENCE_RELEASE",
            "ETL23",
            releaseDateTime
        )

        verify(releaseRepository, never()).save(any())
        verify(eventService, never()).updateReleaseDateAndIapsFlag(any(), any())
        verify(custodyService, never()).addRotlEndDate(any())
        verify(contactRepository, never()).save(any())

        verify(custodyService).updateStatus(
            custody,
            CustodialStatusCode.CUSTODY_ROTL,
            releaseDate,
            "Released on Temporary Licence"
        )
        verify(custodyService).updateLocation(custody, institution, releaseDate)
    }
}
