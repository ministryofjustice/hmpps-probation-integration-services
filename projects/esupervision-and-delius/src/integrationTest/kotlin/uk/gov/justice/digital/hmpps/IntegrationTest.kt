package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonContactDetailsGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.ContactType
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.model.PersonalDetails
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Test
    fun `esupervision received contact created`() {
        val message = MessageGenerator.RECEIVED_A000001
        val notification =
            Notification(message = message, attributes = MessageAttributes(eventType = message.eventType))
        channelManager.getChannel(queueName).publishAndWait(notification)

        val contact = contactRepository.findAll().single {
            it.person.id == PersonGenerator.DEFAULT_PERSON.id && it.description == "Online check in completed"
        }
        assertThat(contact.type.code).isEqualTo(ContactType.E_SUPERVISION_CHECK_IN)
        assertThat(contact.date).isEqualTo(notification.message.occurredAt.toLocalDate())
        assertThat(contact.event.id).isEqualTo(PersonGenerator.DEFAULT_EVENT.id)
        assertThat(contact.provider.id).isEqualTo(ProviderGenerator.DEFAULT_PROVIDER.id)
        assertThat(contact.team.id).isEqualTo(ProviderGenerator.DEFAULT_TEAM.id)
        assertThat(contact.staff.id).isEqualTo(ProviderGenerator.DEFAULT_STAFF.id)
        assertThat(contact.alert).isEqualTo(true)
        assertThat(contact.isSensitive).isEqualTo(false)
        assertThat(contact.notes).isEqualTo("Online check in completed" + System.lineSeparator() + "Review the online check in using the manage probation check ins service: https://esupervision/check-in/received")
    }

    @Test
    fun `esupervision expired contact created`() {
        val message = MessageGenerator.EXPIRED_A000001
        val notification =
            Notification(message = message, attributes = MessageAttributes(eventType = message.eventType))
        channelManager.getChannel(queueName).publishAndWait(notification)

        val contact = contactRepository.findAll().single {
            it.person.id == PersonGenerator.DEFAULT_PERSON.id && it.description == "Check in has not been submitted on time"
        }
        assertThat(contact.type.code).isEqualTo(ContactType.E_SUPERVISION_CHECK_IN)
        assertThat(contact.date).isEqualTo(notification.message.occurredAt.toLocalDate())
        assertThat(contact.event.id).isEqualTo(PersonGenerator.DEFAULT_EVENT.id)
        assertThat(contact.provider.id).isEqualTo(ProviderGenerator.DEFAULT_PROVIDER.id)
        assertThat(contact.team.id).isEqualTo(ProviderGenerator.DEFAULT_TEAM.id)
        assertThat(contact.staff.id).isEqualTo(ProviderGenerator.DEFAULT_STAFF.id)
        assertThat(contact.alert).isEqualTo(true)
        assertThat(contact.isSensitive).isEqualTo(false)
        assertThat(contact.notes).isEqualTo("Check in has not been submitted on time" + System.lineSeparator() + "Review the online check in using the manage probation check ins service: https://esupervision/check-in/expired")
    }

    @Test
    fun `get contact details for a single crn`() {
        mockMvc.perform(get("/case/${PersonContactDetailsGenerator.DEFAULT_PERSON_CONTACT_DETAILS.crn}").withToken())
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """
                    {
                      "crn": "${PersonContactDetailsGenerator.DEFAULT_PERSON_CONTACT_DETAILS.crn}",
                      "name": {
                        "forename": "${PersonContactDetailsGenerator.DEFAULT_PERSON_CONTACT_DETAILS.firstName}",
                        "surname": "${PersonContactDetailsGenerator.DEFAULT_PERSON_CONTACT_DETAILS.lastName}"
                      },
                      "mobile": "${PersonContactDetailsGenerator.DEFAULT_PERSON_CONTACT_DETAILS.mobile.toString()}",
                      "email": "${PersonContactDetailsGenerator.DEFAULT_PERSON_CONTACT_DETAILS.emailAddress}",
                      "practitioner": {
                        "code": "${ProviderGenerator.DEFAULT_STAFF.code}",
                        "email": "john.smith@moj.gov.uk",
                        "name": {
                          "forename": "${ProviderGenerator.DEFAULT_STAFF.forename}",
                          "surname": "${ProviderGenerator.DEFAULT_STAFF.surname}"
                        },
                        "localAdminUnit": {
                          "code": "${ProviderGenerator.DEFAULT_LDU.code}",
                          "description": "${ProviderGenerator.DEFAULT_LDU.description}"
                        },
                        "probationDeliveryUnit": {
                          "code": "${ProviderGenerator.DEFAULT_PDU.code}",
                          "description": "${ProviderGenerator.DEFAULT_PDU.description}"
                        },
                        "provider": {
                          "code": "${ProviderGenerator.DEFAULT_PROVIDER.code}",
                          "description": "${ProviderGenerator.DEFAULT_PROVIDER.description}"
                        }
                      }
                    }
                    """.trimIndent(),
                    JsonCompareMode.STRICT,
                )
            )
    }

    @Test
    fun `bad crn returns a 404`() {
        mockMvc.perform(get("/case/NOT_A_CRN").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `get multiple contact details`() {
        val crns = listOf(
            PersonContactDetailsGenerator.DEFAULT_PERSON_CONTACT_DETAILS.crn,
            PersonContactDetailsGenerator.DEFAULT_PERSON_CONTACT_DETAILS.crn,
        )
        mockMvc.perform(post("/cases").withJson(crns).withToken())
            .andExpect(status().isOk)
            .andExpect { jsonPath("$.length()").value(2) }
    }

    @Test
    fun `get multiple contact details with one bad crn returns only existing crn details`() {
        val crns = listOf(
            PersonContactDetailsGenerator.DEFAULT_PERSON_CONTACT_DETAILS.crn,
            "NOT_A_CRN",
        )
        mockMvc.perform(post("/cases").withJson(crns).withToken())
            .andExpect(status().isOk)
            .andExpect { jsonPath("$.length()").value(1) }
    }

    @Test
    fun `validate a correct set of details and return a 200 response status code`() {
        val testBody = PersonalDetails(
            crn = PersonGenerator.DEFAULT_PERSON.crn,
            name = uk.gov.justice.digital.hmpps.model.Name(
                forename = PersonGenerator.DEFAULT_PERSON.firstName,
                surname = PersonGenerator.DEFAULT_PERSON.lastName
            ),
            dateOfBirth = PersonGenerator.DEFAULT_PERSON.dateOfBirth
        )
        mockMvc.perform(
            post("/case/{${PersonGenerator.DEFAULT_PERSON.crn}/validate-details").withJson(testBody).withToken()
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `validate an correct set of details, with name wrong case and return a 200 response status code`() {
        val testBody = PersonalDetails(
            crn = PersonGenerator.DEFAULT_PERSON.crn,
            name = uk.gov.justice.digital.hmpps.model.Name(
                forename = PersonGenerator.DEFAULT_PERSON.firstName.uppercase(),
                surname = PersonGenerator.DEFAULT_PERSON.lastName.uppercase()
            ),
            dateOfBirth = PersonGenerator.DEFAULT_PERSON.dateOfBirth
        )
        mockMvc.perform(
            post("/case/{${PersonGenerator.DEFAULT_PERSON.crn}/validate-details").withJson(testBody).withToken()
        ).andExpect(status().isOk)
    }

    @Test
    fun `validate an incorrect set of details and return a 400 response status code`() {
        val testBody = PersonalDetails(
            crn = PersonGenerator.DEFAULT_PERSON.crn,
            name = uk.gov.justice.digital.hmpps.model.Name(
                forename = "WrongForename",
                surname = "WrongSurname"
            ),
            dateOfBirth = PersonGenerator.DEFAULT_PERSON.dateOfBirth
        )
        mockMvc.perform(
            post("/case/{${PersonGenerator.DEFAULT_PERSON.crn}/validate-details").withJson(testBody).withToken()
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `validate details for a non existent crn and return a 404 response status code`() {
        val testBody = PersonalDetails(
            crn = "NOT_A_CRN",
            name = uk.gov.justice.digital.hmpps.model.Name(
                forename = "AnyForename",
                surname = "AnySurname"
            ),
            dateOfBirth = LocalDate.parse("2000-01-01")
        )
        mockMvc.perform(post("/case/NOT_A_CRN/validate-details").withJson(testBody).withToken())
            .andExpect(status().isNotFound)
    }
}
