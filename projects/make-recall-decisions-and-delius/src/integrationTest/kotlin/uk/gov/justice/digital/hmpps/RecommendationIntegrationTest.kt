package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import jakarta.persistence.EntityManager
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.atLeastOnce
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.entity.Contact
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@SpringBootTest
internal class RecommendationIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var entityManager: EntityManager

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `management oversight decision to recall`() {
        val notification = prepEvent("management-oversight-recall", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(notification)

        val person = PersonGenerator.DECISION_TO_RECALL
        val contact = getContact(person.id)
        assertNotNull(contact!!)
        assertThat(contact.providerId, equalTo(PersonGenerator.DEFAULT_PROVIDER.id))
        assertThat(contact.teamId, equalTo(PersonGenerator.DEFAULT_TEAM.id))
        assertThat(contact.staffId, equalTo(UserGenerator.WITH_STAFF.staff!!.id))
        assertThat(
            contact.notes,
            equalTo("View details of the Manage a Recall Oversight Decision: http://mrd.case.crn/overview")
        )
        assertThat(contact.isSensitive, equalTo(true))
        verify(telemetryService, atLeastOnce()).notificationReceived(notification)
    }

    @Test
    fun `management oversight decision not to recall`() {
        val notification = prepEvent("management-oversight-not-recall", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(notification)

        val person = PersonGenerator.DECISION_NOT_TO_RECALL
        val contact = getContact(person.id)
        assertNotNull(contact!!)
        assertThat(contact.providerId, equalTo(PersonGenerator.DEFAULT_PROVIDER.id))
        assertThat(contact.teamId, equalTo(PersonGenerator.DEFAULT_TEAM.id))
        assertThat(contact.staffId, equalTo(UserGenerator.WITH_STAFF.staff!!.id))
        assertThat(
            contact.notes,
            equalTo("View details of the Manage a Recall Oversight Decision: http://mrd.case.crn/overview")
        )
        assertThat(contact.isSensitive, equalTo(true))
        verify(telemetryService, atLeastOnce()).notificationReceived(notification)
    }

    @Test
    fun `considering a recall`() {
        val notification = prepEvent("consideration", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(notification)

        val person = PersonGenerator.RECOMMENDATION_STARTED
        val contact = getContact(person.id)
        assertNotNull(contact!!)
        assertThat(contact.providerId, equalTo(PersonGenerator.DEFAULT_PROVIDER.id))
        assertThat(contact.teamId, equalTo(PersonGenerator.DEFAULT_TEAM.id))
        assertThat(contact.staffId, equalTo(UserGenerator.WITH_STAFF.staff!!.id))
        assertThat(
            contact.notes,
            equalTo("I am considering recalling Mr Z because he bridges the licence conditions xyz………")
        )
        assertThat(contact.isSensitive, equalTo(true))
        assertThat(contact.type.code, equalTo("C519"))
        assertThat(contact.outcome, equalTo(null))
        verify(telemetryService, atLeastOnce()).notificationReceived(notification)
    }

    @Test
    fun `recommendation deleted`() {
        val notification = prepEvent("recommendation-deleted", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(notification)

        val person = PersonGenerator.RECOMMENDATION_DELETED
        val contact = getContact(person.id)
        assertNotNull(contact!!)
        assertThat(contact.providerId, equalTo(PersonGenerator.DEFAULT_PROVIDER.id))
        assertThat(contact.teamId, equalTo(PersonGenerator.DEFAULT_TEAM.id))
        assertThat(contact.staffId, equalTo(UserGenerator.WITH_STAFF.staff!!.id))
        assertThat(contact.notes, equalTo("my rationale for deleting the case"))
        assertThat(contact.isSensitive, equalTo(true))
        verify(telemetryService, atLeastOnce()).notificationReceived(notification)
    }

    @Test
    fun `recommendation deleted contact is assigned to manager when provided staff member is no longer active`() {
        val notification = prepEvent("recommendation-deleted-inactive-staff", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(notification)

        val contact = getContact(PersonGenerator.RECOMMENDATION_DELETED_INACTIVE_STAFF.id)!!
        assertThat(contact.staffId, equalTo(PersonGenerator.DEFAULT_STAFF.id))
    }

    private fun getContact(personId: Long) = entityManager
        .createQuery("select c from Contact c where c.personId = :personId", Contact::class.java)
        .setParameter("personId", personId)
        .resultList.firstOrNull()
}
