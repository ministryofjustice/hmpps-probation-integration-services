package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.data.IapsPersonRepository
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.assessment.entity.OasysAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getByCrn
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader.notification
import uk.gov.justice.digital.hmpps.service.Risk
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.Duration
import java.time.LocalDate

@SpringBootTest
internal class IntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var personRepository: PersonRepository

    @Autowired
    lateinit var registrationRepository: RegistrationRepository

    @Autowired
    lateinit var oasysAssessmentRepository: OasysAssessmentRepository

    @Autowired
    lateinit var iapsPersonRepository: IapsPersonRepository

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `a new assessment is created and existing registrations are deregistered`() {
        val message = notification<HmppsDomainEvent>("assessment-summary-produced-${PersonGenerator.NO_RISK.crn}")
        val prevRegs =
            registrationRepository.findByPersonIdAndTypeFlagCode(
                PersonGenerator.NO_RISK.id,
                ReferenceDataGenerator.DEFAULT_FLAG.code
            )
        assertThat(prevRegs.size, equalTo(2))

        channelManager.getChannel(queueName).publishAndWait(prepNotification(message, wireMockServer.port()))

        val person = personRepository.getByCrn(PersonGenerator.NO_RISK.crn)
        assertNull(person.highestRiskColour)

        val registrations =
            registrationRepository.findByPersonIdAndTypeFlagCode(person.id, ReferenceDataGenerator.DEFAULT_FLAG.code)
        assertTrue(registrations.isEmpty())

        val assessment = oasysAssessmentRepository.findAll().firstOrNull { it.person.id == person.id }
        assertThat(assessment?.court?.code, equalTo("CRT150"))
        assertThat(assessment?.offence?.code, equalTo("80400"))
        assertThat(assessment?.assessedBy, equalTo("John Smith"))
        assertThat(assessment?.date, equalTo(LocalDate.parse("2023-12-07")))
        assertThat(assessment?.totalScore, equalTo(76))
    }

    @Test
    fun `an existing assessment is replaced, a new registration added and iaps is notified`() {
        val message = notification<HmppsDomainEvent>("assessment-summary-produced-${PersonGenerator.LOW_RISK.crn}")
        val prevRegs =
            registrationRepository.findByPersonIdAndTypeFlagCode(
                PersonGenerator.LOW_RISK.id,
                ReferenceDataGenerator.DEFAULT_FLAG.code
            )
        assertThat(prevRegs.size, equalTo(1))

        channelManager.getChannel(queueName)
            .publishAndWait(prepNotification(message, wireMockServer.port()), Duration.ofMinutes(3))

        val person = personRepository.getByCrn(PersonGenerator.LOW_RISK.crn)
        assertThat(person.highestRiskColour, equalTo("Green"))

        val registrations =
            registrationRepository.findByPersonIdAndTypeFlagCode(person.id, ReferenceDataGenerator.DEFAULT_FLAG.code)
        assertThat(registrations.size, equalTo(1))
        val reg = registrations.first()
        assertThat(reg.type.code, equalTo(RegistrationGenerator.TYPES[Risk.L.code]?.code))

        val assessment = oasysAssessmentRepository.findAll().firstOrNull { it.person.id == person.id }
        assertThat(assessment?.court?.code, equalTo("CRT150"))
        assertThat(assessment?.offence?.code, equalTo("80400"))
        assertThat(assessment?.assessedBy, equalTo("John Smith"))
        assertThat(assessment?.date, equalTo(LocalDate.parse("2023-12-07")))
        assertThat(assessment?.totalScore, equalTo(94))

        val iaps = iapsPersonRepository.findAll().firstOrNull { it.personId == person.id }
        assertThat(iaps?.iapsFlag, equalTo(true))
    }

    @Test
    fun `a new assessment is created and an existing registration matches the current risk`() {
        val message = notification<HmppsDomainEvent>("assessment-summary-produced-${PersonGenerator.HIGH_RISK.crn}")

        channelManager.getChannel(queueName)
            .publishAndWait(prepNotification(message, wireMockServer.port()))

        val person = personRepository.getByCrn(PersonGenerator.HIGH_RISK.crn)
        assertThat(person.highestRiskColour, equalTo("Orange"))

        val registrations =
            registrationRepository.findByPersonIdAndTypeFlagCode(person.id, ReferenceDataGenerator.DEFAULT_FLAG.code)
        assertThat(registrations.size, equalTo(1))
        val reg = registrations.first()
        assertThat(reg.type.code, equalTo(RegistrationGenerator.TYPES[Risk.H.code]?.code))

        val assessment = oasysAssessmentRepository.findAll().firstOrNull { it.person.id == person.id }
        assertThat(assessment?.court?.code, equalTo("LVRPCC"))
        assertThat(assessment?.offence?.code, equalTo("00857"))
        assertThat(assessment?.assessedBy, equalTo("LevelTwo CentralSupport"))
        assertThat(assessment?.date, equalTo(LocalDate.parse("2023-12-15")))
        assertThat(assessment?.totalScore, equalTo(108))
    }
}
