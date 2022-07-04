package uk.gov.justice.digital.hmpps

import io.specto.hoverfly.junit.core.Hoverfly
import io.specto.hoverfly.junit.core.HoverflyMode
import io.specto.hoverfly.junit5.HoverflyExtension
import io.specto.hoverfly.junit5.api.HoverflyConfig
import io.specto.hoverfly.junit5.api.HoverflyCore
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.stringContainsInOrder
import org.junit.jupiter.api.Assertions.assertNotNull
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
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteNomisTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.PrisonCaseNoteGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.StaffRepository

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

    @Autowired
    private lateinit var staffRepository: StaffRepository

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
        val nomisCaseNote = PrisonCaseNoteGenerator.EXISTING_IN_BOTH
        val original = caseNoteRepository.findByNomisId(nomisCaseNote.eventId)

        jmsTemplate.convertSendAndWait(queueName, CaseNoteMessageGenerator.EXISTS_IN_DELIUS)

        val saved = caseNoteRepository.findByNomisId(nomisCaseNote.eventId)

        assertThat(
            saved?.notes,
            stringContainsInOrder(
                original?.notes,
                nomisCaseNote.type,
                nomisCaseNote.subType,
                nomisCaseNote.text,
                nomisCaseNote.amendments[0].authorName,
                DeliusDateTimeFormatter.format(nomisCaseNote.amendments[0].creationDateTime),
                nomisCaseNote.amendments[0].additionalNoteText
            )
        )
    }

    @Test
    fun `create a new case note succesfully`() {
        val nomisCaseNote = PrisonCaseNoteGenerator.NEW_TO_DELIUS
        val original = caseNoteRepository.findByNomisId(nomisCaseNote.eventId)
        assertNull(original)

        jmsTemplate.convertSendAndWait(queueName, CaseNoteMessageGenerator.NEW_TO_DELIUS)

        val saved = caseNoteRepository.findByNomisId(nomisCaseNote.eventId)
        assertNotNull(saved)

        assertThat(
            saved!!.notes,
            stringContainsInOrder(nomisCaseNote.type, nomisCaseNote.subType, nomisCaseNote.text)
        )

        assertThat(
            saved.type.code,
            equalTo(CaseNoteNomisTypeGenerator.NEG.type.code)
        )

        assertThat(
            saved.eventId,
            equalTo(EventGenerator.CUSTODIAL_EVENT.id)
        )

        val staff = staffRepository.findById(saved.staffId).orElseThrow()
        assertThat(staff.code, equalTo("${ProbationAreaGenerator.DEFAULT.code}B001"))

        assertThat(saved.createdByUserId, equalTo(UserGenerator.APPLICATION_USER.id))
        assertThat(saved.lastModifiedUserId, equalTo(UserGenerator.APPLICATION_USER.id))
    }
}
