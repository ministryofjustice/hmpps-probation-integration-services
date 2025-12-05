package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}") private val queueName: String,
    private val mockMvc: MockMvc,
    private val channelManager: HmppsChannelManager,
    private val contactRepository: ContactRepository,
    private val wireMockServer: WireMockServer
) {

    @Test
    fun `esupervision received contact created`() {
        val message = MessageGenerator.RECEIVED_A000001
        val notification =
            Notification(message = message, attributes = MessageAttributes(eventType = message.eventType))
        channelManager.getChannel(queueName).publishAndWait(notification)

        val contact = contactRepository.findAll().single {
            it.person.id == PersonGenerator.DEFAULT_PERSON.id && it.description == "Online check in completed" && it.externalReference == null
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
            it.person.id == PersonGenerator.DEFAULT_PERSON.id && it.description == "Check in has not been submitted on time" && it.externalReference == null
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
    fun `esupervision received with detail url`() {
        val notification = prepEvent("esupervision-received-detail-url-A000001", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(notification)

        val contact = contactRepository.findAll().single {
            it.externalReference == "urn:uk:gov:hmpps:esupervision:check-in:61e80782-bdd1-43c3-b69f-78366582210a"
        }
        assertThat(contact.description).isEqualTo("Online check in completed")
        assertThat(contact.type.code).isEqualTo(ContactType.E_SUPERVISION_CHECK_IN)
        assertThat(contact.event.id).isEqualTo(PersonGenerator.DEFAULT_EVENT.id)
        assertThat(contact.provider.id).isEqualTo(ProviderGenerator.DEFAULT_PROVIDER.id)
        assertThat(contact.team.id).isEqualTo(ProviderGenerator.DEFAULT_TEAM.id)
        assertThat(contact.staff.id).isEqualTo(ProviderGenerator.DEFAULT_STAFF.id)
        assertThat(contact.alert).isEqualTo(true)
        assertThat(contact.isSensitive).isEqualTo(false)
        assertThat(contact.notes).isEqualTo(
            """
            |Online check in completed
            |Some notes about the check-in
            """.trimMargin()
        )
    }

    @Test
    fun `esupervision expired with detail url`() {
        val notification = prepEvent("esupervision-expired-detail-url-A000001", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(notification)

        val contact = contactRepository.findAll().single {
            it.externalReference == "urn:uk:gov:hmpps:esupervision:check-in-expiry:93d51990-3b72-4a7b-97cb-92759b04eaeb"
        }
        assertThat(contact.description).isEqualTo("Check in has not been submitted on time")
        assertThat(contact.type.code).isEqualTo(ContactType.E_SUPERVISION_CHECK_IN)
        assertThat(contact.event.id).isEqualTo(PersonGenerator.DEFAULT_EVENT.id)
        assertThat(contact.provider.id).isEqualTo(ProviderGenerator.DEFAULT_PROVIDER.id)
        assertThat(contact.team.id).isEqualTo(ProviderGenerator.DEFAULT_TEAM.id)
        assertThat(contact.staff.id).isEqualTo(ProviderGenerator.DEFAULT_STAFF.id)
        assertThat(contact.alert).isEqualTo(true)
        assertThat(contact.isSensitive).isEqualTo(false)
        assertThat(contact.notes).isEqualTo(
            """
            |Check in has not been submitted on time
            |Some notes about the expired check-in
            """.trimMargin()
        )
    }

    @Test
    fun `get contact details for a single crn`() {
        mockMvc.get("/case/${PersonContactDetailsGenerator.PERSON_CONTACT_DETAILS_1.crn}") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "crn": "${PersonContactDetailsGenerator.PERSON_CONTACT_DETAILS_1.crn}",
                          "name": {
                            "forename": "${PersonContactDetailsGenerator.PERSON_CONTACT_DETAILS_1.firstName}",
                            "surname": "${PersonContactDetailsGenerator.PERSON_CONTACT_DETAILS_1.lastName}"
                          },
                          "mobile": "${PersonContactDetailsGenerator.PERSON_CONTACT_DETAILS_1.mobile.toString()}",
                          "email": "${PersonContactDetailsGenerator.PERSON_CONTACT_DETAILS_1.emailAddress}",
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
                }
            }
    }

    @Test
    fun `bad crn returns a 404`() {
        mockMvc.get("/case/NOT_A_CRN") { withToken() }
            .andExpect { status().isNotFound }
    }

    @Test
    fun `get multiple contact details`() {
        val crns = listOf(
            PersonContactDetailsGenerator.PERSON_CONTACT_DETAILS_1.crn,
            PersonContactDetailsGenerator.PERSON_CONTACT_DETAILS_2.crn,
        )
        mockMvc.post("/cases") {
            json = crns
            withToken()
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(2) }
            }

            .andDo { print() }
    }

    @Test
    fun `get multiple contact details with one bad crn returns only existing crn details`() {
        val crns = listOf(
            PersonContactDetailsGenerator.DEFAULT_PERSON_CONTACT_DETAILS.crn,
            "NOT_A_CRN",
        )
        mockMvc.post("/cases") {
            json = crns
            withToken()
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(1) }
            }
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
        mockMvc.post("/case/{${PersonGenerator.DEFAULT_PERSON.crn}/validate-details") {
            json = testBody
            withToken()
        }
            .andExpect { status { isOk() } }
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
        mockMvc.post("/case/{${PersonGenerator.DEFAULT_PERSON.crn}/validate-details") {
            json = testBody
            withToken()
        }
            .andExpect { status { isOk() } }
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
        mockMvc.post("/case/{${PersonGenerator.DEFAULT_PERSON.crn}/validate-details") {
            json = testBody
            withToken()
        }
            .andExpect { status { isBadRequest() } }
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
        mockMvc.post("/case/NOT_A_CRN/validate-details") {
            json = testBody
            withToken()
        }
            .andExpect { status { isNotFound() } }
    }
}
