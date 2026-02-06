package uk.gov.justice.digital.hmpps

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class BaseIntegrationTest {
    @Autowired
    internal lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var personRepository: PersonRepository

    @Autowired
    internal lateinit var transactionTemplate: TransactionTemplate

    @Autowired
    internal lateinit var entityManager: EntityManager

    @MockitoBean
    internal lateinit var telemetryService: TelemetryService
}