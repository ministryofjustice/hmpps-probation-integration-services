package uk.gov.justice.digital.hmpps

import io.specto.hoverfly.junit.core.Hoverfly
import io.specto.hoverfly.junit.core.HoverflyMode
import io.specto.hoverfly.junit5.HoverflyExtension
import io.specto.hoverfly.junit5.api.HoverflyConfig
import io.specto.hoverfly.junit5.api.HoverflyCore
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.stringContainsInOrder
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.SimulationBuilder
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteMessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.NomisCaseNoteGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository

@ActiveProfiles("integration-test")
@SpringBootTest
@HoverflyCore(
    mode = HoverflyMode.SIMULATE,
    config = HoverflyConfig(adminPort = 8888, proxyPort = 8500, webServer = true)
)
@ExtendWith(HoverflyExtension::class)
class IntegrationTest {

    @Value("\${spring.jms.template.default-destination}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var simBuilder: SimulationBuilder

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Autowired
    private lateinit var caseNoteRepository: CaseNoteRepository

    @BeforeEach
    fun setUp(hoverfly: Hoverfly) {
        val sources = simBuilder.simulationsFromFile()
        if (sources.isNotEmpty()) {
            hoverfly.simulate(
                sources.first(),
                *sources.drop(1).toTypedArray()
            )
        }
    }

    @Test
    fun `update an existing case note succesfully`() {
        val nomisCaseNote = NomisCaseNoteGenerator.EXISTING_IN_BOTH
        val original = caseNoteRepository.findByNomisId(nomisCaseNote.eventId)

        jmsTemplate.convertSendAndWait(queueName, CaseNoteMessageGenerator.EXISTS_IN_DELIUS)

        val saved = caseNoteRepository.findByNomisId(nomisCaseNote.eventId)

        assertThat(
            saved?.notes,
            stringContainsInOrder(original?.notes, nomisCaseNote.type, nomisCaseNote.subType, nomisCaseNote.text)
        )
    }

    @Test
    fun `create a new case note succesfully`() {
        val nomisCaseNote = NomisCaseNoteGenerator.NEW_TO_DELIUS
        val original = caseNoteRepository.findByNomisId(nomisCaseNote.eventId)
        assertNull(original)

        jmsTemplate.convertSendAndWait(queueName, CaseNoteMessageGenerator.NEW_TO_DELIUS)

        val saved = caseNoteRepository.findByNomisId(nomisCaseNote.eventId)

        assertThat(
            saved?.notes,
            stringContainsInOrder(nomisCaseNote.type, nomisCaseNote.subType, nomisCaseNote.text)
        )
    }
}
