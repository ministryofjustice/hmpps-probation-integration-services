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
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RecallReasonGenerator
import uk.gov.justice.digital.hmpps.data.generator.withManager
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.LicenceConditionService
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.Recall
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReason
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.messaging.ActionResult
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
        val prisonerMovement =
            PrisonerMovement.Received(
                custody.disposal.event.person.nomsNumber,
                InstitutionGenerator.DEFAULT.nomisCdeCode!!,
                PrisonerMovement.Type.ADMISSION,
                "",
                ZonedDateTime.now(),
            )
        val ex =
            assertThrows<IgnorableMessageException> {
                action.accept(PrisonerMovementContext(prisonerMovement, custody))
            }
        assertThat(ex.message, equalTo("RecallNotRequired"))
    }

    @ParameterizedTest
    @MethodSource("invalidRecallDates")
    fun `ignored when recall date not valid`(date: ZonedDateTime) {
        val prisonerMovement =
            PrisonerMovement.Received(
                nomsId,
                InstitutionGenerator.DEFAULT.nomisCdeCode!!,
                PrisonerMovement.Type.ADMISSION,
                "",
                date,
            )
        val custody =
            EventGenerator.previouslyReleasedEvent(
                PersonGenerator.generate(nomsId),
                InstitutionGenerator.DEFAULT,
                CustodialStatusCode.RELEASED_ON_LICENCE,
                releaseDate = ZonedDateTime.now().minusDays(7),
            ).disposal!!.custody!!

        val ex =
            assertThrows<IgnorableMessageException> {
                action.accept(PrisonerMovementContext(prisonerMovement, custody))
            }
        assertThat(ex.message, equalTo("InvalidRecallDate"))
    }

    @ParameterizedTest
    @MethodSource("transferredCases")
    fun `recall created correctly for transfers`(
        prisonerMovement: PrisonerMovement,
        custody: Custody,
        rrc: RecallReason.Code,
    ) {
        doAnswer { RecallReasonGenerator.generate(it.getArgument(0)) }
            .whenever(recallReasonRepository).findByCode(any())
        doAnswer<Recall> { it.getArgument(0) }.whenever(recallRepository).save(any())

        val res = action.accept(PrisonerMovementContext(prisonerMovement, custody))
        assertThat(res, instanceOf(ActionResult.Success::class.java))
        val success = res as ActionResult.Success
        assertThat(success.type, equalTo(ActionResult.Type.Recalled))
        val recall = argumentCaptor<Recall>()
        verify(recallRepository).save(recall.capture())
        assertThat(recall.firstValue.reason.code, equalTo(rrc.value))
        verify(contactService).createContact(any(), any(), any(), any(), anyOrNull())
    }

    @Test
    fun `non int transfers are ignored`() {
    }

    companion object {
        private val nomsId = "R1234AC"

        @JvmStatic
        fun nonRecallableCustodies() =
            listOf(
                EventGenerator.previouslyRecalledEvent(
                    PersonGenerator.RELEASABLE,
                    InstitutionGenerator.DEFAULT,
                ).disposal!!.custody,
                EventGenerator.custodialEvent(
                    PersonGenerator.generate(nomsId),
                    InstitutionGenerator.DEFAULT,
                ).disposal!!.custody,
            ) +
                CustodialStatusCode.entries.filter {
                    it !in
                        listOf(
                            CustodialStatusCode.RELEASED_ON_LICENCE,
                            CustodialStatusCode.CUSTODY_ROTL,
                        )
                }.map {
                    EventGenerator.previouslyReleasedEvent(
                        PersonGenerator.generate(nomsId),
                        InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.IN_COMMUNITY],
                        it,
                    ).disposal!!.custody
                }

        @JvmStatic
        fun invalidRecallDates() =
            listOf(
                ZonedDateTime.now().plusDays(1),
                ZonedDateTime.now().minusDays(14),
            )

        @JvmStatic
        fun transferredCases(): List<Arguments> {
            val movement =
                PrisonerMovement.Received(
                    nomsId,
                    InstitutionGenerator.DEFAULT.nomisCdeCode!!,
                    PrisonerMovement.Type.TRANSFERRED,
                    "INT",
                    ZonedDateTime.now(),
                )
            return listOf(
                Arguments.of(
                    movement,
                    EventGenerator.previouslyReleasedEvent(
                        PersonGenerator.RECALLABLE,
                        InstitutionGenerator.DEFAULT,
                        CustodialStatusCode.CUSTODY_ROTL,
                    ).withManager().disposal!!.custody,
                    RecallReason.Code.END_OF_TEMPORARY_LICENCE,
                ),
                Arguments.of(
                    movement,
                    EventGenerator.previouslyReleasedEvent(
                        PersonGenerator.RECALLABLE,
                        InstitutionGenerator.DEFAULT,
                        CustodialStatusCode.RELEASED_ON_LICENCE,
                    ).withManager().disposal!!.custody,
                    RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT,
                ),
            )
        }
    }
}
