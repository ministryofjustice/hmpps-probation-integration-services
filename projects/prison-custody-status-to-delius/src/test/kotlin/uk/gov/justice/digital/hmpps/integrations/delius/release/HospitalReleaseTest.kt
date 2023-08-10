package uk.gov.justice.digital.hmpps.integrations.delius.release

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.FeatureFlagCodes
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.custodialEvent
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RecallReasonGenerator
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReason
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovement
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@ExtendWith(MockitoExtension::class)
class HospitalReleaseTest : ReleaseServiceTestBase() {

    @Test
    fun unsupportedReleaseTypeIsIgnored() {
        whenever(featureFlags.enabled(FeatureFlagCodes.HOSPITAL_RELEASE)).thenReturn(false)

        assertThrows<IgnorableMessageException> {
            releaseService.release(
                PrisonerMovement.Received(
                    "",
                    "",
                    "RELEASED",
                    "HO",
                    ZonedDateTime.now()
                )
            )
        }
        assertThrows<IgnorableMessageException> {
            releaseService.release(
                PrisonerMovement.Received(
                    "",
                    "",
                    "RELEASED",
                    "HQ",
                    ZonedDateTime.now()
                )
            )
        }
        assertThrows<IgnorableMessageException> {
            releaseService.release(
                PrisonerMovement.Received(
                    "",
                    "",
                    "RELEASED_TO_HOSPITAL",
                    "HP",
                    ZonedDateTime.now()
                )
            )
        }
    }

    @Test
    fun `release from hospital when in custody`() {
        val person = PersonGenerator.RELEASABLE
        val institution = InstitutionGenerator.DEFAULT
        val event = custodialEvent(person, institution)
        val releaseDateTime = ZonedDateTime.now()
        val releaseDate = releaseDateTime.truncatedTo(ChronoUnit.DAYS)
        val custody = event.disposal!!.custody!!

        whenever(featureFlags.enabled(FeatureFlagCodes.HOSPITAL_RELEASE)).thenReturn(true)
        whenever(institutionRepository.findByNomisCdeCode(InstitutionGenerator.DEFAULT.nomisCdeCode!!))
            .thenReturn(institution)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))

        releaseService.release(
            PrisonerMovement.Released(
                person.nomsNumber,
                institution.nomisCdeCode,
                "RELEASED",
                "HQ",
                releaseDateTime
            )
        )

        verify(releaseRepository, never()).save(any())
        verify(contactRepository, never()).save(any())
        verify(custodyService).updateStatus(
            custody,
            CustodialStatusCode.IN_CUSTODY,
            releaseDate,
            "Transfer to/from Hospital"
        )
        verify(custodyService, never()).updateLocation(any(), any(), any(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `terminated status create no action`() {
        val institution = InstitutionGenerator.DEFAULT
        val person = PersonGenerator.RELEASABLE
        val event = custodialEvent(person, institution, custodialStatusCode = CustodialStatusCode.TERMINATED)
        val releaseDateTime = ZonedDateTime.now()

        whenever(featureFlags.enabled(FeatureFlagCodes.HOSPITAL_RELEASE)).thenReturn(true)
        whenever(institutionRepository.findByNomisCdeCode(InstitutionGenerator.DEFAULT.nomisCdeCode!!))
            .thenReturn(institution)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))

        val ex = assertThrows<IgnorableMessageException> {
            releaseService.release(
                PrisonerMovement.Released(
                    person.nomsNumber,
                    institution.nomisCdeCode,
                    "RELEASED",
                    "HO",
                    releaseDateTime
                )
            )
        }

        assertThat(ex.message, equalTo("NoActionHospitalRelease"))
        verify(releaseRepository, never()).save(any())
        verify(contactRepository, never()).save(any())
        verify(custodyService, never()).updateStatus(any(), any(), any(), any())
        verify(custodyService, never()).updateLocation(any(), any(), any(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `release from hospital when in community`() {
        val person = PersonGenerator.RECALLABLE
        val event = custodialEvent(
            person,
            InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY]!!,
            custodialStatusCode = CustodialStatusCode.RELEASED_ON_LICENCE
        )
        val releaseDateTime = ZonedDateTime.now()
        val releaseDate = releaseDateTime.truncatedTo(ChronoUnit.DAYS)
        val institution = InstitutionGenerator.DEFAULT
        val custody = event.disposal!!.custody!!

        whenever(featureFlags.enabled(FeatureFlagCodes.HOSPITAL_RELEASE)).thenReturn(true)
        whenever(institutionRepository.findByNomisCdeCode(InstitutionGenerator.DEFAULT.nomisCdeCode!!))
            .thenReturn(institution)
        whenever(eventService.getActiveCustodialEvents(person.nomsNumber)).thenReturn(listOf(event))
        whenever(recallReasonRepository.findByCode(RecallReason.Code.TRANSFER_TO_SECURE_HOSPITAL.value))
            .thenReturn(RecallReasonGenerator.generate(RecallReason.Code.TRANSFER_TO_SECURE_HOSPITAL.value))

        releaseService.release(
            PrisonerMovement.Released(
                person.nomsNumber,
                institution.nomisCdeCode,
                "RELEASED_TO_HOSPITAL",
                "HP",
                releaseDateTime
            )
        )

        verify(releaseRepository, never()).save(any())
        verify(contactRepository, never()).save(any())
        verify(recallService).createRecall(any(), any(), any(), anyOrNull())
        verify(custodyService).updateStatus(
            custody,
            CustodialStatusCode.RECALLED,
            releaseDate,
            "Transfer to/from Hospital"
        )
        verify(custodyService).updateLocation(custody, institution, releaseDate)
    }
}
