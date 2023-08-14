package uk.gov.justice.digital.hmpps.messaging.actions

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.data.generator.CustodyGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.PrisonManagerService
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.messaging.ActionResult
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovement
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

    @Test
    fun `when in correct location no changes made`() {
        val custody = custody()
        val prisonerMovement = PrisonerMovement.Received(
            custody.disposal.event.person.nomsNumber,
            "WSI",
            PrisonerMovement.Type.TRANSFERRED,
            "INT",
            ZonedDateTime.now()
        )

        val res = action.accept(PrisonerMovementContext(prisonerMovement, custody))
        assertInstanceOf(ActionResult.Ignored::class.java, res)
        val ignored = res as ActionResult.Ignored
        assertThat(ignored.reason, equalTo("PrisonerLocationCorrect"))
    }

    private fun custody(): Custody {
        val person = PersonGenerator.generate("T1234ST")
        return CustodyGenerator.generate(person, InstitutionGenerator.DEFAULT, CustodialStatusCode.IN_CUSTODY)
    }
}
