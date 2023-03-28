package uk.gov.justice.digital.hmpps.integrations.delius.recall

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.CustodyService
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventService
import uk.gov.justice.digital.hmpps.integrations.delius.event.manager.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.LicenceConditionService
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.PrisonManagerService
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.RecallReasonRepository

@ExtendWith(MockitoExtension::class)
abstract class RecallServiceTestBase {
    @Mock
    lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    lateinit var featureFlags: FeatureFlags

    @Mock
    lateinit var eventService: EventService

    @Mock
    lateinit var institutionRepository: InstitutionRepository

    @Mock
    lateinit var recallReasonRepository: RecallReasonRepository

    @Mock
    lateinit var recallRepository: RecallRepository

    @Mock
    lateinit var custodyService: CustodyService

    @Mock
    lateinit var licenceConditionService: LicenceConditionService

    @Mock
    lateinit var orderManagerRepository: OrderManagerRepository

    @Mock
    lateinit var personManagerRepository: PersonManagerRepository

    @Mock
    lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var contactAlertRepository: ContactAlertRepository

    @Mock
    lateinit var prisonManagerService: PrisonManagerService

    @InjectMocks
    lateinit var recallService: RecallService
}
