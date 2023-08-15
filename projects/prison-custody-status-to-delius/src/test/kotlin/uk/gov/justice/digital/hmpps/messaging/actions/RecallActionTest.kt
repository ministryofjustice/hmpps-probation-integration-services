package uk.gov.justice.digital.hmpps.messaging.actions

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.LicenceConditionService
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovement
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovementContext
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class RecallActionTest {
    @Mock
    internal lateinit var recallReasonRepository: RecallReasonRepository

    @Mock
    internal lateinit var recallRepository: RecallRepository

    @Mock
    internal lateinit var licenceConditionService: LicenceConditionService

    @Mock
    internal lateinit var contactService: ContactService

    @InjectMocks
    internal lateinit var action: RecallAction

    @ParameterizedTest
    @MethodSource("nonRecallableCustodies")
    fun `ignored when recall not possible`(custody: Custody) {
        val prisonerMovement = PrisonerMovement.Received(
            custody.disposal.event.person.nomsNumber,
            InstitutionGenerator.DEFAULT.nomisCdeCode!!,
            PrisonerMovement.Type.ADMISSION,
            "",
            ZonedDateTime.now()
        )
        val ex = assertThrows<IgnorableMessageException> {
            action.accept(PrisonerMovementContext(prisonerMovement, custody))
        }
        assertThat(ex.message, equalTo("RecallNotPossible"))
    }

    @ParameterizedTest
    @MethodSource("invalidRecallDates")
    fun `ignored when recall date not valid`(date: ZonedDateTime) {
        val prisonerMovement = PrisonerMovement.Received(
            nomsId,
            InstitutionGenerator.DEFAULT.nomisCdeCode!!,
            PrisonerMovement.Type.ADMISSION,
            "",
            date
        )
        val custody = EventGenerator.previouslyReleasedEvent(
            PersonGenerator.generate(nomsId),
            InstitutionGenerator.DEFAULT,
            CustodialStatusCode.RELEASED_ON_LICENCE,
            releaseDate = ZonedDateTime.now().minusDays(7)
        ).disposal!!.custody!!

        val ex = assertThrows<IgnorableMessageException> {
            action.accept(PrisonerMovementContext(prisonerMovement, custody))
        }
        assertThat(ex.message, equalTo("InvalidRecallDate"))
    }

    companion object {

        private val nomsId = "R1234AC"

        @JvmStatic
        fun nonRecallableCustodies() = listOf(
            EventGenerator.previouslyRecalledEvent(
                PersonGenerator.RELEASABLE,
                InstitutionGenerator.DEFAULT
            ).disposal!!.custody,
            EventGenerator.custodialEvent(
                PersonGenerator.generate(nomsId),
                InstitutionGenerator.DEFAULT
            ).disposal!!.custody
        ) + CustodialStatusCode.entries.filter {
            it !in listOf(
                CustodialStatusCode.RELEASED_ON_LICENCE,
                CustodialStatusCode.CUSTODY_ROTL
            )
        }.map {
            EventGenerator.previouslyReleasedEvent(
                PersonGenerator.generate(nomsId),
                InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY],
                it
            ).disposal!!.custody
        }

        @JvmStatic
        fun invalidRecallDates() = listOf(
            ZonedDateTime.now().plusDays(1),
            ZonedDateTime.now().minusDays(14)
        )
    }
}
