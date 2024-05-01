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
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.entity.NsiManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.entity.NsiSubType
import uk.gov.justice.digital.hmpps.integrations.delius.entity.NsiType
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.entity.getByCrnOrNoms
import uk.gov.justice.digital.hmpps.messaging.FeatureFlag
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

    @MockBean
    lateinit var featureFlags: FeatureFlags

    @Order(1)
    @Test
    fun `process opd assessment`() {
        whenever(featureFlags.enabled(FeatureFlag)).thenReturn(true)
        val message = prepMessage("opd-assessment-new", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(message)

        val com = personManagerRepository.getByCrnOrNoms(PersonGenerator.PERSON_OPD_NEW.crn, null)
        val nsi = nsiRepository.findNsiByPersonIdAndTypeCode(com.person.id, NsiType.Code.OPD_COMMUNITY_PATHWAY.value)
        assertNotNull(nsi!!)
        assertThat(nsi.type.code, equalTo(NsiType.Code.OPD_COMMUNITY_PATHWAY.value))
        assertThat(nsi.subType?.code, equalTo(NsiSubType.Code.COMMUNITY_PATHWAY.value))
        assertThat(nsi.status.code, equalTo(NsiStatus.Code.PENDING_CONSULTATION.value))
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
            .firstOrNull { it.personId == com.person.id && it.type.code == ContactType.Code.PENDING_CONSULTATION.value }
        assertNotNull(opdContact!!)
        assertThat(opdContact.nsiId, equalTo(nsi.id))

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
        whenever(featureFlags.enabled(FeatureFlag)).thenReturn(true)
        val message = prepMessage("opd-assessment-update", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(message)

        val com = personManagerRepository.getByCrnOrNoms(null, PersonGenerator.PERSON_OPD_NEW.nomsId)
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
            .firstOrNull { it.personId == com.person.id && it.type.code == ContactType.Code.PENDING_CONSULTATION.value }
        assertNotNull(opdContact!!)
    }

    @Test
    fun `does not process opd assessment when feature flagged`() {
        whenever(featureFlags.enabled(FeatureFlag)).thenReturn(false)

        val message = prepMessage("opd-assessment-new", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(message)

        verify(telemetryService).trackEvent(
            "OpdAssessmentIgnored",
            mapOf(
                "crn" to PersonGenerator.PERSON_OPD_NEW.crn,
                "date" to "30/10/2023 16:42:25",
                "result" to "Screened In"
            )
        )
    }
}
