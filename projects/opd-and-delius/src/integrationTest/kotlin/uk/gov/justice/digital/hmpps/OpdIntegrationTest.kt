package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.NsiManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.NsiSubType
import uk.gov.justice.digital.hmpps.integrations.delius.NsiType
import uk.gov.justice.digital.hmpps.integrations.delius.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.getByCrn
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class OpdIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var personManagerRepository: PersonManagerRepository

    @Autowired
    lateinit var nsiRepository: NsiRepository

    @Autowired
    lateinit var nsiManagerRepository: NsiManagerRepository

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Order(1)
    @Test
    fun `process opd assessment`() {
        val message = prepMessage("opd-assessment-new", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(message)

        val com = personManagerRepository.getByCrn(PersonGenerator.PERSON_OPD_NEW.crn)
        val nsi = nsiRepository.findNsiByPersonIdAndTypeCode(com.person.id, NsiType.Code.OPD_COMMUNITY_PATHWAY.value)
        assertNotNull(nsi!!)
        assertThat(nsi.type.code, equalTo(NsiType.Code.OPD_COMMUNITY_PATHWAY.value))
        assertThat(nsi.subType?.code, equalTo(NsiSubType.Code.COMMUNITY_PATHWAY.value))
        assertThat(nsi.status.code, equalTo(NsiStatus.Code.READY_FOR_SERVICE.value))
        assertThat(
            nsi.notes,
            containsString(
                """
            |OPD Assessment Date: 30/10/2023 16:42:25
            |OPD Result: Screened In
            |This notes entry was automatically created by the system
                """.trimMargin()
            )
        )

        val nsiManager = nsiManagerRepository.findAll().firstOrNull { it.nsi.id == nsi.id }
        assertNotNull(nsiManager!!)
        assertThat(nsiManager.teamId, equalTo(com.teamId))
        assertThat(nsiManager.staffId, equalTo(com.staffId))

        val opdContact = contactRepository.findAll()
            .firstOrNull { it.personId == com.person.id && it.type.code == ContactType.Code.READY_FOR_SERVICES.value }
        assertNotNull(opdContact!!)

        verify(telemetryService).trackEvent(
            "OpdAssessmentScreenedIn",
            mapOf(
                "crn" to PersonGenerator.PERSON_OPD_NEW.crn,
                "date" to "30/10/2023 16:42:25",
                "result" to "Screened In"
            )
        )
    }

    @Order(2)
    @Test
    fun `process update to opd assessment`() {
        val message = prepMessage("opd-assessment-update", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(message)

        val com = personManagerRepository.getByCrn(PersonGenerator.PERSON_OPD_NEW.crn)
        val nsi = nsiRepository.findNsiByPersonIdAndTypeCode(com.person.id, NsiType.Code.OPD_COMMUNITY_PATHWAY.value)
        assertNotNull(nsi!!)
        assertThat(
            nsi.notes,
            containsString(
                """
            |OPD Assessment Date: 31/10/2023 13:42:25
            |OPD Result: Screened In - with override
            |This notes entry was automatically created by the system
                """.trimMargin()
            )
        )

        val opdContact = contactRepository.findAll()
            .firstOrNull { it.personId == com.person.id && it.type.code == ContactType.Code.READY_FOR_SERVICES.value }
        assertNotNull(opdContact!!)
    }
}
