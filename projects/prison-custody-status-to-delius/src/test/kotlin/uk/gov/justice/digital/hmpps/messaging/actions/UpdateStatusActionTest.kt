package uk.gov.justice.digital.hmpps.messaging.actions

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasEntry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.data.generator.CustodyGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
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

    @Test
    fun `unexpected status for hospital release is recorded`() {
        val custody = custody()
        val prisonerMovement = PrisonerMovement.Released(
            custody.disposal.event.person.nomsNumber,
            "UNK",
            PrisonerMovement.Type.RELEASED_TO_HOSPITAL,
            "",
            ZonedDateTime.now()
        )

        val ex = assertThrows<IgnorableMessageException> {
            action.accept(PrisonerMovementContext(prisonerMovement, custody))
        }
        assertThat(ex.message, equalTo("NoActionHospitalRelease"))
        assertThat(
            ex.additionalProperties,
            hasEntry("currentStatusCode", CustodialStatusCode.POST_SENTENCE_SUPERVISION.code)
        )
        assertThat(
            ex.additionalProperties,
            hasEntry("currentLocation", InstitutionCode.UNKNOWN.code)
        )
    }

    private fun custody(): Custody {
        val person = PersonGenerator.generate("T1234ST")
        return CustodyGenerator.generate(
            person,
            requireNotNull(InstitutionGenerator.STANDARD_INSTITUTIONS[InstitutionCode.UNKNOWN]),
            CustodialStatusCode.POST_SENTENCE_SUPERVISION
        )
    }
}
