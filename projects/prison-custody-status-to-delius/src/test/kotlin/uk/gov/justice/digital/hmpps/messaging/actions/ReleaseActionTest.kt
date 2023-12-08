package uk.gov.justice.digital.hmpps.messaging.actions

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.CustodyGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.withManager
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventService
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.host.entity.HostRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.release.entity.Release
import uk.gov.justice.digital.hmpps.integrations.delius.release.entity.ReleaseRepository
import uk.gov.justice.digital.hmpps.messaging.ActionResult
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovement
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovementContext
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class ReleaseActionTest {
    @Mock
    internal lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    internal lateinit var institutionRepository: InstitutionRepository

    @Mock
    internal lateinit var hostRepository: HostRepository

    @Mock
    internal lateinit var releaseRepository: ReleaseRepository

    @Mock
    internal lateinit var contactService: ContactService

    @Mock
    internal lateinit var eventService: EventService

    @InjectMocks
    internal lateinit var action: ReleaseAction

    @ParameterizedTest
    @MethodSource("invalidDatesForRelease")
    fun `invalid release date is recorded with no changes`(custody: Custody) {
        val prisonerMovement =
            PrisonerMovement.Released(
                custody.disposal.event.person.nomsNumber,
                InstitutionGenerator.DEFAULT.nomisCdeCode!!,
                PrisonerMovement.Type.RELEASED,
                "",
                ZonedDateTime.now().minusDays(1),
            )

        val ex =
            assertThrows<IgnorableMessageException> {
                action.accept(PrisonerMovementContext(prisonerMovement, custody))
            }
        assertThat(ex.message, equalTo("InvalidReleaseDate"))
    }

    @ParameterizedTest
    @MethodSource("nonReleasableStatuses")
    fun `unable to release if not in releasable state`(statusCode: CustodialStatusCode) {
        val event = EventGenerator.custodialEvent(PersonGenerator.RELEASABLE, InstitutionGenerator.DEFAULT, statusCode)
        val prisonerMovement =
            PrisonerMovement.Released(
                event.person.nomsNumber,
                InstitutionGenerator.DEFAULT.nomisCdeCode,
                PrisonerMovement.Type.RELEASED,
                "",
                ZonedDateTime.now().minusDays(1),
            )
        withReferenceData(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.ADULT_LICENCE]!!)
        whenever(institutionRepository.findByNomisCdeCode(InstitutionGenerator.DEFAULT.nomisCdeCode!!))
            .thenReturn(InstitutionGenerator.DEFAULT)

        val res = action.accept(PrisonerMovementContext(prisonerMovement, event.disposal!!.custody!!))
        assertThat(res, instanceOf(ActionResult.Ignored::class.java))
        val ignored = res as ActionResult.Ignored
        assertThat(ignored.reason, equalTo("UnableToRelease"))
    }

    @Test
    fun `uses delius institution if not provided or not found`() {
        val event =
            EventGenerator.custodialEvent(PersonGenerator.RELEASABLE, InstitutionGenerator.DEFAULT).withManager()
        val prisonerMovement =
            PrisonerMovement.Released(
                event.person.nomsNumber,
                "NonExistent",
                PrisonerMovement.Type.RELEASED,
                "",
                ZonedDateTime.now().minusDays(1),
            )
        withReferenceData(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.ADULT_LICENCE]!!)

        val res = action.accept(PrisonerMovementContext(prisonerMovement, event.disposal!!.custody!!))
        assertThat(res, instanceOf(ActionResult.Success::class.java))
        val success = res as ActionResult.Success
        assertThat(success.type, equalTo(ActionResult.Type.Released))
        val release = argumentCaptor<Release>()
        verify(releaseRepository).save(release.capture())
        assertThat(release.firstValue.institutionId, equalTo(event.disposal?.custody?.institution?.id))
        verify(contactService).createContact(any(), any(), any(), any(), anyOrNull())
    }

    @ParameterizedTest
    @MethodSource("nonReleasableMovementTypes")
    fun `unable to release if movement type not expected`(type: PrisonerMovement.Type) {
        val custody =
            CustodyGenerator.generate(
                PersonGenerator.RELEASABLE,
                InstitutionGenerator.DEFAULT,
                CustodialStatusCode.IN_CUSTODY,
            )
        val prisonerMovement =
            PrisonerMovement.Released(
                custody.disposal.event.person.nomsNumber,
                InstitutionGenerator.DEFAULT.nomisCdeCode,
                type,
                "",
                ZonedDateTime.now().minusDays(1),
            )

        val ex = assertThrows<IgnorableMessageException> { action.accept(PrisonerMovementContext(prisonerMovement, custody)) }
        assertThat(ex.message, equalTo("UnsupportedReleaseType"))
    }

    @Test
    fun `can create release even if no custody institution set`() {
        val event =
            EventGenerator.custodialEvent(PersonGenerator.RELEASABLE, null).withManager()
        val prisonerMovement =
            PrisonerMovement.Released(
                event.person.nomsNumber,
                null,
                PrisonerMovement.Type.RELEASED,
                "",
                ZonedDateTime.now().minusDays(1),
            )
        withReferenceData(ReferenceDataGenerator.RELEASE_TYPE[ReleaseTypeCode.ADULT_LICENCE]!!)
        whenever(institutionRepository.findByCode(InstitutionCode.UNKNOWN.code))
            .thenReturn(InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.UNKNOWN])

        val res = action.accept(PrisonerMovementContext(prisonerMovement, event.disposal!!.custody!!))
        assertThat(res, instanceOf(ActionResult.Success::class.java))
        val success = res as ActionResult.Success
        assertThat(success.type, equalTo(ActionResult.Type.Released))
        val release = argumentCaptor<Release>()
        verify(releaseRepository).save(release.capture())
        assertThat(
            release.firstValue.institutionId,
            equalTo(InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.UNKNOWN]?.id),
        )
        verify(contactService).createContact(any(), any(), any(), any(), anyOrNull())
    }

    companion object {
        @JvmStatic
        fun invalidDatesForRelease() =
            listOf(
                EventGenerator.custodialEvent(
                    PersonGenerator.RELEASABLE,
                    InstitutionGenerator.DEFAULT,
                    disposalDate = ZonedDateTime.now(),
                ).disposal!!.custody,
                EventGenerator.previouslyRecalledEvent(
                    PersonGenerator.RELEASABLE,
                    InstitutionGenerator.DEFAULT,
                    recallDate = ZonedDateTime.now(),
                ).disposal!!.custody,
            )

        @JvmStatic
        fun nonReleasableStatuses() =
            listOf(
                CustodialStatusCode.RELEASED_ON_LICENCE,
                CustodialStatusCode.CUSTODY_ROTL,
                CustodialStatusCode.TERMINATED,
                CustodialStatusCode.AUTO_TERMINATED,
                CustodialStatusCode.IN_CUSTODY_IRC,
                CustodialStatusCode.POST_SENTENCE_SUPERVISION,
            )

        @JvmStatic
        fun nonReleasableMovementTypes() = PrisonerMovement.Type.entries.filter { it != PrisonerMovement.Type.RELEASED }
    }

    private fun withReferenceData(vararg referenceData: ReferenceData) {
        referenceData.forEach {
            whenever(referenceDataRepository.findByCodeAndSetName(it.code, it.set.name))
                .thenReturn(it)
        }
    }
}
