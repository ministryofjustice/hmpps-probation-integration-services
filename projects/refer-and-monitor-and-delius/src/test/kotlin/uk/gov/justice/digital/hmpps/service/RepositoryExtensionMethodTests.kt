package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exception.ReferralNotFoundException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus
import uk.gov.justice.digital.hmpps.messaging.NsiTermination
import uk.gov.justice.digital.hmpps.messaging.ReferralEndType
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class RepositoryExtensionMethodTests {

    @Mock
    lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    lateinit var nsiRepository: NsiRepository

    @Mock
    lateinit var nsiStatusRepository: NsiStatusRepository

    @Mock
    lateinit var nsiOutcomeRepository: ReferenceDataRepository

    @Mock
    lateinit var statusHistoryRepository: NsiStatusHistoryRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    lateinit var contactOutcomeRepository: ContactOutcomeRepository

    @Mock
    lateinit var createNsi: CreateNsi

    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var featureFlags: FeatureFlags

    @InjectMocks
    lateinit var nsiService: NsiService

    @Test
    fun `nsi not found causes failure`() {
        whenever(nsiRepository.findByPersonCrnAndExternalReference(any(), any())).thenReturn(null)

        val termination = NsiTermination(
            "D123456",
            "urn:fake:test:string",
            1,
            ZonedDateTime.now().minusDays(1),
            ZonedDateTime.now(),
            ReferralEndType.CANCELLED,
            null,
            "Notes",
            ZonedDateTime.now(),
            "End Of Service Report Submitted"
        )
        assertThrows<ReferralNotFoundException> { nsiService.terminateNsi(termination) }
    }

    @Test
    fun `nsi type not found causes failure`() {
        val person = PersonGenerator.DEFAULT
        val nsi = NsiGenerator.WITHDRAWN
        whenever(nsiRepository.findByPersonCrnAndExternalReference(person.crn, nsi.externalReference!!)).thenReturn(nsi)

        val ex = assertThrows<NotFoundException> {
            nsiService.terminateNsi(
                NsiTermination(
                    person.crn,
                    nsi.externalReference!!,
                    1,
                    ZonedDateTime.now().minusDays(1),
                    ZonedDateTime.now(),
                    ReferralEndType.COMPLETED,
                    null,
                    "Notes",
                    ZonedDateTime.now(),
                    "End Of Service Report Submitted"
                )
            )
        }

        assertThat(ex.message, equalTo("NsiStatus with code of COMP not found"))
    }

    @Test
    fun `nsi outcome not found causes failure`() {
        val person = PersonGenerator.DEFAULT
        val nsi = NsiGenerator.WITHDRAWN
        whenever(nsiRepository.findByPersonCrnAndExternalReference(person.crn, nsi.externalReference!!)).thenReturn(nsi)
        whenever(nsiStatusRepository.findByCode(NsiStatus.Code.END.value)).thenReturn(NsiGenerator.COMP_STATUS)

        val ex = assertThrows<NotFoundException> {
            nsiService.terminateNsi(
                NsiTermination(
                    person.crn,
                    nsi.externalReference!!,
                    1,
                    ZonedDateTime.now().minusDays(1),
                    ZonedDateTime.now(),
                    ReferralEndType.COMPLETED,
                    null,
                    "Notes",
                    ZonedDateTime.now(),
                    "End Of Service Report Submitted"
                )
            )
        }

        assertThat(ex.message, equalTo("NsiOutcome with code of CRS03 not found"))
    }
}
