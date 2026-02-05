package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.aspect.DeliusUserAspect
import uk.gov.justice.digital.hmpps.audit.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.AppointmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.SentenceAppointmentRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
open class IntegrationTestBase {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var wireMockServer: WireMockServer

    @Autowired
    protected lateinit var entityManager: EntityManager

    @Autowired
    protected lateinit var transactionTemplate: TransactionTemplate

    @Autowired
    protected lateinit var sentenceAppointmentRepository: SentenceAppointmentRepository

    @Autowired
    protected lateinit var appointmentRepository: AppointmentRepository

    @Autowired
    protected lateinit var contactRepository: ContactRepository

    @Autowired
    protected lateinit var contactAlertRepository: ContactAlertRepository

    @Autowired
    protected lateinit var documentRepository: DocumentRepository

    @Autowired
    lateinit var deliusUserAspect: DeliusUserAspect

    @Value("\${messaging.producer.topic}")
    lateinit var topicName: String

    @Autowired
    lateinit var auditedInteractionRepository: AuditedInteractionRepository

    @Autowired
    lateinit var businessInteractionRepository: BusinessInteractionRepository

    @Autowired
    lateinit var channelManager: HmppsChannelManager
}