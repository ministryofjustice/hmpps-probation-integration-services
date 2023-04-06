package uk.gov.justice.digital.hmpps.integrations.delius.release

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.CustodyService
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventService
import uk.gov.justice.digital.hmpps.integrations.delius.event.manager.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.host.HostRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository

@ExtendWith(MockitoExtension::class)
open class ReleaseServiceTestBase {
    @Mock
    internal lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    internal lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    internal lateinit var institutionRepository: InstitutionRepository

    @Mock
    internal lateinit var hostRepository: HostRepository

    @Mock
    internal lateinit var eventService: EventService

    @Mock
    internal lateinit var releaseRepository: ReleaseRepository

    @Mock
    internal lateinit var custodyService: CustodyService

    @Mock
    internal lateinit var orderManagerRepository: OrderManagerRepository

    @Mock
    internal lateinit var contactRepository: ContactRepository

    @Mock
    internal lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    internal lateinit var personDied: PersonDied

    @Mock
    internal lateinit var featureFlags: FeatureFlags

    @InjectMocks
    internal lateinit var releaseService: ReleaseService
}
