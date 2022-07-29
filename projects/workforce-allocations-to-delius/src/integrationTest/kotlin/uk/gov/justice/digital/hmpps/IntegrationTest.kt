package uk.gov.justice.digital.hmpps

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.integrations.delius.repository.StaffRepository
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

const val CASE_NOTE_MERGE = "CaseNoteMerge"

@ActiveProfiles("integration-test")
@SpringBootTest
class IntegrationTest {

    @Value("\${spring.jms.template.default-destination}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Autowired
    private lateinit var staffRepository: StaffRepository

    @MockBean
    private lateinit var telemetryService: TelemetryService
}
