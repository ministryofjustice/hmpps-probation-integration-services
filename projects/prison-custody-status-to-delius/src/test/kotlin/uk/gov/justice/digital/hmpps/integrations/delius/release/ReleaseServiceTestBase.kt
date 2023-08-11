// package uk.gov.justice.digital.hmpps.integrations.delius.release
//
// import org.junit.jupiter.api.BeforeEach
// import org.junit.jupiter.api.extension.ExtendWith
// import org.mockito.InjectMocks
// import org.mockito.Mock
// import org.mockito.junit.jupiter.MockitoExtension
// import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
// import uk.gov.justice.digital.hmpps.flags.FeatureFlags
// import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactService
// import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactAlertRepository
// import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
// import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactTypeRepository
// import uk.gov.justice.digital.hmpps.integrations.delius.custody.CustodyService
// import uk.gov.justice.digital.hmpps.integrations.delius.event.EventService
// import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.OrderManagerRepository
// import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.entity.PersonManagerRepository
// import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.host.entity.HostRepository
// import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.InstitutionRepository
// import uk.gov.justice.digital.hmpps.integrations.delius.recall.RecallService
// import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReasonRepository
// import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
// import uk.gov.justice.digital.hmpps.integrations.delius.release.entity.ReleaseRepository
//
// @ExtendWith(MockitoExtension::class)
// open class ReleaseServiceTestBase {
//     @Mock
//     internal lateinit var auditedInteractionService: AuditedInteractionService
//
//     @Mock
//     internal lateinit var referenceDataRepository: ReferenceDataRepository
//
//     @Mock
//     internal lateinit var institutionRepository: InstitutionRepository
//
//     @Mock
//     internal lateinit var hostRepository: HostRepository
//
//     @Mock
//     internal lateinit var eventService: EventService
//
//     @Mock
//     internal lateinit var releaseRepository: ReleaseRepository
//
//     @Mock
//     internal lateinit var custodyService: CustodyService
//
//     @Mock
//     internal lateinit var orderManagerRepository: OrderManagerRepository
//
//     @Mock
//     internal lateinit var contactRepository: ContactRepository
//
//     @Mock
//     internal lateinit var contactTypeRepository: ContactTypeRepository
//
//     @Mock
//     internal lateinit var personDied: PersonDied
//
//     @Mock
//     internal lateinit var featureFlags: FeatureFlags
//
//     @Mock
//     internal lateinit var recallReasonRepository: RecallReasonRepository
//
//     @Mock
//     internal lateinit var recallService: RecallService
//
//     @Mock
//     internal lateinit var contactAlertRepository: ContactAlertRepository
//
//     @Mock
//     internal lateinit var personManagerRepository: PersonManagerRepository
//
//     @InjectMocks
//     internal lateinit var contactService: ContactService
//
//     internal lateinit var releaseService: ReleaseService
//
//     @BeforeEach
//     fun setUp() {
//         releaseService = ReleaseService(
//             auditedInteractionService,
//             eventService,
//             personDied,
//             recallReasonRepository,
//             recallService
//         )
//     }
// }
