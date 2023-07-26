package uk.gov.justice.digital.hmpps.integrations.delius.recall

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.CustodyService
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventService
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.LicenceConditionService
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallRepository

@ExtendWith(MockitoExtension::class)
abstract class RecallServiceTestBase {
    @Mock
    lateinit var auditedInteractionService: AuditedInteractionService

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

    @InjectMocks
    lateinit var contactService: ContactService

    lateinit var recallService: RecallService

    @BeforeEach
    fun setUp() {
        recallService = RecallService(
            auditedInteractionService,
            eventService,
            institutionRepository,
            recallReasonRepository,
            recallRepository,
            custodyService,
            licenceConditionService,
            orderManagerRepository,
            personManagerRepository,
            contactService
        )
    }
}
