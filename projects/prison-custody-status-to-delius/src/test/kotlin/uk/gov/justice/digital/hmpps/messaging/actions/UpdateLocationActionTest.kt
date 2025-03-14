package uk.gov.justice.digital.hmpps.messaging.actions

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.custodialEvent
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.previouslyReleasedEvent
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.PrisonManagerService
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.messaging.ActionResult
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovement
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovement.Type.RELEASED
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovement.Type.TRANSFERRED
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovementContext
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class UpdateLocationActionTest {

    @Mock
    internal lateinit var institutionRepository: InstitutionRepository

    @Mock
    internal lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    internal lateinit var custodyRepository: CustodyRepository

    @Mock
    internal lateinit var custodyHistoryRepository: CustodyHistoryRepository

    @Mock
    internal lateinit var prisonManagerService: PrisonManagerService

    @Mock
    internal lateinit var contactService: ContactService

    @InjectMocks
    internal lateinit var action: UpdateLocationAction

    @ParameterizedTest
    @MethodSource("noChangeMovements")
    fun `no changes made when location is correct`(custody: Custody, prisonerMovement: PrisonerMovement) {
        if (prisonerMovement.isAbsconded()) {
            whenever(institutionRepository.findByNomisCdeCode(InstitutionGenerator.DEFAULT.nomisCdeCode!!))
                .thenReturn(InstitutionGenerator.DEFAULT)
            whenever(institutionRepository.findByCode(InstitutionCode.UNLAWFULLY_AT_LARGE.code))
                .thenReturn(InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.UNLAWFULLY_AT_LARGE])
        }

        val res = action.accept(PrisonerMovementContext(prisonerMovement, custody))
        assertInstanceOf(ActionResult.Ignored::class.java, res)
        val ignored = res as ActionResult.Ignored
        assertThat(ignored.reason, equalTo("PrisonerLocationCorrect"))
    }

    @Test
    fun `updates location correctly when different from existing and creates contact`() {
        whenever(institutionRepository.findByNomisCdeCode(InstitutionGenerator.MOVED_TO.nomisCdeCode!!))
            .thenReturn(InstitutionGenerator.MOVED_TO)
        whenever(
            referenceDataRepository.findByCodeAndSetName(
                CustodyEventTypeCode.LOCATION_CHANGE.code,
                "CUSTODY EVENT TYPE"
            )
        ).thenReturn(ReferenceDataGenerator.CUSTODY_EVENT_TYPE[CustodyEventTypeCode.LOCATION_CHANGE])

        val res = action.accept(PrisonerMovementContext(received.copy(toPrisonId = "SWI"), custody()))
        assertThat(res, instanceOf(ActionResult.Success::class.java))
        val success = res as ActionResult.Success
        assertThat(success.type, equalTo(ActionResult.Type.LocationUpdated))
    }

    @Test
    fun `updates pom when being released if different from existing pom`() {
        whenever(institutionRepository.findByNomisCdeCode(InstitutionGenerator.DEFAULT.nomisCdeCode!!))
            .thenReturn(InstitutionGenerator.DEFAULT)
        val community = InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY]!!
        whenever(institutionRepository.findByCode(community.code)).thenReturn(community)
        whenever(
            referenceDataRepository.findByCodeAndSetName(
                CustodyEventTypeCode.LOCATION_CHANGE.code,
                "CUSTODY EVENT TYPE"
            )
        ).thenReturn(ReferenceDataGenerator.CUSTODY_EVENT_TYPE[CustodyEventTypeCode.LOCATION_CHANGE])

        val res = action.accept(PrisonerMovementContext(released, custody()))
        assertThat(res, instanceOf(ActionResult.Success::class.java))
        val success = res as ActionResult.Success
        assertThat(success.type, equalTo(ActionResult.Type.LocationUpdated))
        verify(prisonManagerService).allocateToProbationArea(
            any(),
            eq(InstitutionGenerator.DEFAULT.probationArea!!),
            eq(released.occurredAt)
        )
    }

    @ParameterizedTest
    @MethodSource("invalidDatesForRelease")
    fun `invalid release date does not update location`(custody: Custody) {
        val prisonerMovement = PrisonerMovement.Released(
            custody.disposal.event.person.nomsNumber,
            InstitutionGenerator.DEFAULT.nomisCdeCode!!,
            "OUT",
            PrisonerMovement.Type.RELEASED,
            "",
            ZonedDateTime.now().minusDays(1)
        )

        val result = action.accept(PrisonerMovementContext(prisonerMovement, custody))

        assertThat(result, instanceOf(ActionResult.Ignored::class.java))
    }

    @ParameterizedTest
    @MethodSource("invalidRecallDates")
    fun `ignored when recall date not valid`(date: ZonedDateTime) {
        val nomsId = "A1234BC"
        val prisonerMovement = PrisonerMovement.Received(
            nomsId,
            "OUT",
            InstitutionGenerator.DEFAULT.nomisCdeCode!!,
            PrisonerMovement.Type.ADMISSION,
            "",
            date
        )
        val custody = previouslyReleasedEvent(
            PersonGenerator.generate(nomsId),
            InstitutionGenerator.DEFAULT,
            CustodialStatusCode.RELEASED_ON_LICENCE,
            releaseDate = ZonedDateTime.now().minusDays(7)
        ).disposal!!.custody!!

        val res = action.accept(PrisonerMovementContext(prisonerMovement, custody))
        assertThat(res, instanceOf(ActionResult.Ignored::class.java))
    }

    companion object {
        private val received = PrisonerMovement.Received(
            custody().disposal.event.person.nomsNumber,
            "OUT",
            InstitutionGenerator.DEFAULT.nomisCdeCode!!,
            TRANSFERRED,
            "INT",
            ZonedDateTime.now()
        )
        private val released = PrisonerMovement.Released(
            custody().disposal.event.person.nomsNumber,
            InstitutionGenerator.DEFAULT.nomisCdeCode!!,
            "OUT",
            RELEASED,
            "",
            ZonedDateTime.now()
        )

        @JvmStatic
        fun invalidDatesForRelease() = listOf(
            custodialEvent(
                PersonGenerator.RELEASABLE,
                InstitutionGenerator.DEFAULT,
                disposalDate = ZonedDateTime.now()
            ).disposal!!.custody,
            EventGenerator.previouslyRecalledEvent(
                PersonGenerator.RELEASABLE,
                InstitutionGenerator.DEFAULT,
                recallDate = ZonedDateTime.now()
            ).disposal!!.custody
        )

        @JvmStatic
        fun invalidRecallDates() = listOf(
            ZonedDateTime.now().plusDays(1),
            ZonedDateTime.now().minusDays(14)
        )

        @JvmStatic
        fun noChangeMovements() = listOf(
            Arguments.of(custody(), received),
            Arguments.of(released(), released),
            Arguments.of(absconded(), released.copy(type = RELEASED, reason = "UAL")),
            Arguments.of(absconded(), released.copy(type = RELEASED, reason = "UAL_ECL"))
        )

        private fun custody(): Custody {
            val person = PersonGenerator.generate("T1234ST")
            return custodialEvent(person, InstitutionGenerator.DEFAULT, CustodialStatusCode.IN_CUSTODY)
                .withManager().disposal!!.custody!!
        }

        private fun released(): Custody {
            val person = PersonGenerator.generate("R1234SD")
            val event = previouslyReleasedEvent(
                person,
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY],
                CustodialStatusCode.RELEASED_ON_LICENCE
            )
            return requireNotNull(event.disposal?.custody)
        }

        private fun absconded(): Custody {
            val person = PersonGenerator.generate("A1234CD")
            val event = custodialEvent(
                person,
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.UNLAWFULLY_AT_LARGE],
                CustodialStatusCode.IN_CUSTODY
            )
            return requireNotNull(event.disposal?.custody)
        }
    }
}
