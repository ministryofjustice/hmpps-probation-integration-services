package uk.gov.justice.digital.hmpps.messaging.actions

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.CustodyGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.messaging.ActionResult
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovement
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovementContext
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class UpdateStatusActionTest {
    @Mock
    internal lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    internal lateinit var custodyRepository: CustodyRepository

    @Mock
    internal lateinit var custodyHistoryRepository: CustodyHistoryRepository

    @InjectMocks
    internal lateinit var action: UpdateStatusAction

    @ParameterizedTest
    @MethodSource("noChangeStatuses")
    fun `no change when status is correct`(prisonerMovement: PrisonerMovement, custody: Custody) {
        val res = action.accept(PrisonerMovementContext(prisonerMovement, custody))
        assertThat(res, instanceOf(ActionResult.Ignored::class.java))
        val ignored = res as ActionResult.Ignored
        assertThat(ignored.reason, equalTo("PrisonerStatusCorrect"))
    }

    @Test
    fun `unexpected status for hospital release is recorded`() {
        val person = PersonGenerator.generate("T1234ST")
        val custody = CustodyGenerator.generate(
            person,
            InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.UNKNOWN],
            CustodialStatusCode.POST_SENTENCE_SUPERVISION
        )
        val prisonerMovement = PrisonerMovement.Released(
            custody.disposal.event.person.nomsNumber,
            "UNK",
            "OUT",
            PrisonerMovement.Type.RELEASED_TO_HOSPITAL,
            "",
            ZonedDateTime.now()
        )

        val ex = assertThrows<IgnorableMessageException> {
            action.accept(PrisonerMovementContext(prisonerMovement, custody))
        }
        assertThat(ex.message, equalTo("PrisonerStatusCorrect"))
    }

    @Test
    fun `hospital release when released in delius results in recall status`() {
        val person = PersonGenerator.generate("R1234CL")
        val custody = EventGenerator.previouslyReleasedEvent(
            person,
            InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY]
        ).disposal!!.custody!!
        val prisonerMovement = PrisonerMovement.Released(
            custody.disposal.event.person.nomsNumber,
            InstitutionGenerator.DEFAULT.nomisCdeCode!!,
            "OUT",
            PrisonerMovement.Type.RELEASED_TO_HOSPITAL,
            "HP",
            ZonedDateTime.now()
        )
        withReferenceData(
            ReferenceDataGenerator.CUSTODIAL_STATUS[CustodialStatusCode.RECALLED]!!,
            ReferenceDataGenerator.CUSTODY_EVENT_TYPE[CustodyEventTypeCode.STATUS_CHANGE]!!
        )

        val res = action.accept(PrisonerMovementContext(prisonerMovement, custody))
        assertThat(res, instanceOf(ActionResult.Success::class.java))
        val success = res as ActionResult.Success
        assertThat(success.type, equalTo(ActionResult.Type.StatusUpdated))
        verify(custodyRepository).save(any())
        verify(custodyHistoryRepository).save(any())
    }

    companion object {

        private const val NOMS_ID = "T1234ST"
        private fun custody(csc: CustodialStatusCode) = CustodyGenerator.generate(
            PersonGenerator.generate(NOMS_ID),
            InstitutionGenerator.DEFAULT,
            csc
        )

        private fun prisonerReceived(type: PrisonerMovement.Type) = PrisonerMovement.Received(
            NOMS_ID,
            InstitutionGenerator.MOVED_TO.nomisCdeCode!!,
            InstitutionGenerator.DEFAULT.nomisCdeCode!!,
            type,
            "",
            ZonedDateTime.now()
        )

        private val noChangeTypes = listOf(
            CustodialStatusCode.POST_SENTENCE_SUPERVISION,
            CustodialStatusCode.IN_CUSTODY,
            CustodialStatusCode.IN_CUSTODY_IRC
        )

        private val receivedTypes = listOf(
            PrisonerMovement.Type.ADMISSION,
            PrisonerMovement.Type.TRANSFERRED,
            PrisonerMovement.Type.RETURN_FROM_COURT,
            PrisonerMovement.Type.TEMPORARY_ABSENCE_RETURN
        )

        @JvmStatic
        fun noChangeStatuses() = noChangeTypes.flatMap { csc ->
            receivedTypes.map { Arguments.of(prisonerReceived(it), custody(csc)) }
        }
    }

    private fun withReferenceData(vararg referenceData: ReferenceData) {
        referenceData.forEach {
            whenever(referenceDataRepository.findByCodeAndSetName(it.code, it.set.name))
                .thenReturn(it)
        }
    }
}
