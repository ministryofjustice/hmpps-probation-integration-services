package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.ReferralStarted
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import java.time.ZonedDateTime
import java.util.UUID

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ReferralStartedIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var nsiRepository: NsiRepository

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Test
    fun `nsi, manager and contacts created when it doesn't already exist`() {
        val person = PersonGenerator.SETENCED_WITHOUT_NSI
        val referralId = UUID.randomUUID()
        val request = ReferralStarted(
            referralId,
            ZonedDateTime.now(),
            "ACC",
            SentenceGenerator.SENTENCE_WITHOUT_NSI.eventId,
            "The referral has been started"
        )
        val json = objectMapper.readTree(objectMapper.writeValueAsString(request)) as ObjectNode
        json.put("referralId", referralId.toString())

        mockMvc
            .perform(
                MockMvcRequestBuilders.put("/probation-case/${person.crn}/referrals")
                    .withOAuth2Token(wireMockServer)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.toPrettyString())
            ).andExpect(MockMvcResultMatchers.status().isNoContent)

        val nsi = nsiRepository.findByPersonCrnAndExternalReference(person.crn, request.urn)
        assertNotNull(nsi!!)
        assertThat(nsi.referralDate, isCloseTo(request.startedAt))
        assertThat(nsi.actualStartDate!!, isCloseTo(request.startedAt))
        assertThat(nsi.statusDate, isCloseTo(request.startedAt))
        assertThat(nsi.status.code, equalTo(NsiStatus.Code.IN_PROGRESS.value))
        assertThat(nsi.type.code, equalTo("CRS01"))
        assertThat(nsi.eventId, equalTo(SentenceGenerator.SENTENCE_WITHOUT_NSI.eventId))
        assertNotNull(nsi.requirementId)

        val manager = nsi.manager
        assertThat(manager.providerId, equalTo(ProviderGenerator.INTENDED_PROVIDER.id))
        assertThat(manager.teamId, equalTo(ProviderGenerator.INTENDED_TEAM.id))
        assertThat(manager.staffId, equalTo(ProviderGenerator.INTENDED_STAFF.id))
        assertThat(manager.startDate, isCloseTo(request.startedAt))

        val contacts = contactRepository.findAll().filter { it.person.crn == person.crn }
        assertThat(contacts.size, equalTo(3))
        assertThat(
            contacts.map { it.type.code },
            hasItems(
                ContactType.Code.NSI_REFERRAL.value,
                ContactType.Code.NSI_COMMENCED.value,
                ContactType.Code.IN_PROGRESS.value
            )
        )
    }
}
