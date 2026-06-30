package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.entity.ContactOutcome
import uk.gov.justice.digital.hmpps.entity.ContactRepository
import uk.gov.justice.digital.hmpps.entity.ContactType
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.messaging.Handler
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.model.PersonalDetails
import uk.gov.justice.digital.hmpps.service.CheckInService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
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
    private val checkInService: CheckInService,
    private val wireMockServer: WireMockServer,
    private val handler: Handler,
    @MockitoBean private val telemetryService: TelemetryService,
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
        assertThat(contact.event?.id).isEqualTo(EventGenerator.EVENT_2.id)
        assertThat(contact.provider.id).isEqualTo(ProviderGenerator.DEFAULT_PROVIDER.id)
        assertThat(contact.team.id).isEqualTo(ProviderGenerator.DEFAULT_TEAM.id)
        assertThat(contact.staff.id).isEqualTo(ProviderGenerator.DEFAULT_STAFF.id)
        assertThat(contact.isSensitive).isEqualTo(false)
        assertThat(contact.notes).isEqualTo("Review the online check in using the manage probation check ins service: https://esupervision/check-in/received")
        verify(telemetryService).trackEvent(
            "CheckInEventReceived",
            mapOf(
                "eventType" to "esupervision.check-in.received",
                "crn" to PersonGenerator.DEFAULT_PERSON.crn,
                "eventNumber" to EventGenerator.EVENT_2.number,
                "checkInUrl" to "https://esupervision/check-in/received",
                "setupId" to null,
            )
        )
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
        assertThat(contact.event?.id).isEqualTo(EventGenerator.EVENT_2.id)
        assertThat(contact.provider.id).isEqualTo(ProviderGenerator.DEFAULT_PROVIDER.id)
        assertThat(contact.team.id).isEqualTo(ProviderGenerator.DEFAULT_TEAM.id)
        assertThat(contact.staff.id).isEqualTo(ProviderGenerator.DEFAULT_STAFF.id)
        assertThat(contact.isSensitive).isEqualTo(false)
        assertThat(contact.notes).isEqualTo("Review the online check in using the manage probation check ins service: https://esupervision/check-in/expired")
    }

    @Test
    fun `esupervision received falls back to most recent active event when event number is not provided`() {
        val person = PersonGenerator.FALLBACK_EVENT_PERSON
        val message = MessageGenerator.RECEIVED_A000001.copy(
            personReference = PersonReference(listOf(PersonIdentifier("CRN", person.crn))),
            additionalInformation = MessageGenerator.RECEIVED_A000001.additionalInformation - "eventNumber"
        )
        val notification =
            Notification(message = message, attributes = MessageAttributes(eventType = message.eventType))

        channelManager.getChannel(queueName).publishAndWait(notification)

        val contact = contactRepository.findAll()
            .single { it.person.id == person.id && it.description == "Online check in completed" }
        assertThat(contact.event?.id).isEqualTo(EventGenerator.FALLBACK_EVENT_2.id)
        assertThat(contact.isSensitive).isEqualTo(false)
    }

    @Test
    fun `esupervision received contact created when no active event exists`() {
        val message = MessageGenerator.RECEIVED_A000004
        val notification =
            Notification(message = message, attributes = MessageAttributes(eventType = message.eventType))

        channelManager.getChannel(queueName).publishAndWait(notification)

        val contact = contactRepository.findAll()
            .single { it.person.id == PersonGenerator.NO_ACTIVE_EVENT_PERSON.id && it.description == "Online check in completed" }
        assertThat(contact.event?.id).isEqualTo(EventGenerator.INACTIVE_EVENT.id)
        assertThat(contact.event?.active).isFalse()
        assertThat(contact.isSensitive).isEqualTo(false)
        assertThat(contact.notes).isEqualTo("Review the online check in using the manage probation check ins service: https://esupervision/check-in/received")
    }

    @Test
    fun `esupervision received throws an error when no event number is provided and there is no active event`() {
        val message = MessageGenerator.RECEIVED_A000004.copy(
            additionalInformation = MessageGenerator.RECEIVED_A000004.additionalInformation - "eventNumber"
        )

        assertThatThrownBy { checkInService.receiveCheckIn(message) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Case does not have an active event")
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
        assertThat(contact.event?.id).isEqualTo(EventGenerator.EVENT_2.id)
        assertThat(contact.provider.id).isEqualTo(ProviderGenerator.DEFAULT_PROVIDER.id)
        assertThat(contact.team.id).isEqualTo(ProviderGenerator.DEFAULT_TEAM.id)
        assertThat(contact.staff.id).isEqualTo(ProviderGenerator.DEFAULT_STAFF.id)
        assertThat(contact.isSensitive).isEqualTo(false)
        assertThat(contact.notes).isEqualTo("Some notes about the check-in")
    }

    @Test
    fun `esupervision received with detail url has sensitive flag`() {
        val notification = prepEvent("esupervision-received-detail-url-A000007", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(notification)

        val contact = contactRepository.findAll().single {
            it.externalReference == "urn:uk:gov:hmpps:esupervision:check-in:23531be1-335a-428b-8097-d911bf199ae9"
        }

        assertThat(contact.isSensitive).isEqualTo(true)
    }

    @Test
    fun `esupervision reviewed contact updated`() {
        val notification = prepEvent("esupervision-reviewed-A000001", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(notification)

        val contact = contactRepository.findAll().single {
            it.externalReference == "urn:uk:gov:hmpps:esupervision:check-in:8b8a8cf1-a8fe-42c4-879c-095bbed91466"
        }

        assertThat(contact.isSensitive).isEqualTo(false)
        assertThat(contact.notes).isEqualTo(
            """
            |Existing Notes
            |Check-in review
            """.trimMargin()
        )
    }

    @Test
    fun `esupervision updated contact updated`() {
        val notification = prepEvent("esupervision-updated-A000001", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(notification)

        val contact = contactRepository.findAll().single {
            it.externalReference == "urn:uk:gov:hmpps:esupervision:check-in:a18648f4-46ec-4344-8e8e-ba15c18c3ab9"
        }

        assertThat(contact.isSensitive).isEqualTo(true)
        assertThat(contact.notes).isEqualTo(
            """
            |Existing Notes
            |Check-in updated
            """.trimMargin()
        )
    }

    @Test
    fun `esupervision updated expiry contact updated`() {
        val notification = prepEvent("esupervision-updated-expiry-A000001", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(notification)

        val contact = contactRepository.findAll().single {
            it.externalReference == "urn:uk:gov:hmpps:esupervision:check-in-expiry:b5a4d4c6-15c5-4f54-8ec2-f7f38c6f8b23"
        }

        assertThat(contact.isSensitive).isEqualTo(false)
        assertThat(contact.notes).isEqualTo(
            """
            |Existing Notes
            |Check-in updated expiry
            """.trimMargin()
        )
    }

    @Test
    fun `esupervision setup completed contact created`() {
        val message = MessageGenerator.SETUP_COMPLETED_A000001
        val notification =
            Notification(message = message, attributes = MessageAttributes(eventType = message.eventType))

        channelManager.getChannel(queueName).publishAndWait(notification)

        val contact = contactRepository.findAll().single {
            it.externalReference == "urn:uk:gov:hmpps:esupervision:setup:05fb6498-897c-46b4-9614-57c1fc6647bd"
        }

        assertThat(contact.type.code).isEqualTo(ContactType.E_SUPERVISION_SETUP_COMPLETED)
        assertThat(contact.outcome?.code).isEqualTo(ContactOutcome.SETUP_COMPLETED)
        assertThat(contact.date).isEqualTo(notification.message.occurredAt.toLocalDate())
        assertThat(contact.person.id).isEqualTo(PersonGenerator.DEFAULT_PERSON.id)
        assertThat(contact.event?.id).isEqualTo(EventGenerator.EVENT_2.id)
        assertThat(contact.provider.id).isEqualTo(ProviderGenerator.DEFAULT_PROVIDER.id)
        assertThat(contact.team.id).isEqualTo(ProviderGenerator.DEFAULT_TEAM.id)
        assertThat(contact.staff.id).isEqualTo(ProviderGenerator.DEFAULT_STAFF.id)
        assertThat(contact.description).isNull()
        assertThat(contact.notes).isNull()
        verify(telemetryService).trackEvent(
            "CheckInSetupCompleted",
            mapOf(
                "eventType" to "esupervision.setup.completed",
                "crn" to PersonGenerator.DEFAULT_PERSON.crn,
                "eventNumber" to EventGenerator.EVENT_2.number,
                "checkInUrl" to null,
                "setupId" to "05fb6498-897c-46b4-9614-57c1fc6647bd",
            )
        )
    }

    @Test
    fun `esupervision setup removed contact updated`() {
        val originalContact = contactRepository.findAll().single {
            it.externalReference == "urn:uk:gov:hmpps:esupervision:setup:5b487c04-974d-44ca-b8c2-c95053d82479"
        }
        assertThat(originalContact.outcome?.code).isEqualTo(ContactOutcome.SETUP_COMPLETED)

        val removedEvent = prepMessage(MessageGenerator.SETUP_REMOVED_A000001)
        channelManager.getChannel(queueName).publishAndWait(removedEvent)

        val updatedContact = contactRepository.findAll().single { it.id == originalContact.id }
        assertThat(updatedContact.id).isEqualTo(originalContact.id)
        assertThat(updatedContact.type.code).isEqualTo(ContactType.E_SUPERVISION_SETUP_COMPLETED)
        assertThat(updatedContact.outcome?.code).isEqualTo(ContactOutcome.SETUP_REMOVED)
        assertThat(updatedContact.date).isEqualTo(originalContact.date)
        assertThat(updatedContact.startTime).isEqualTo(originalContact.startTime)
        verify(telemetryService).trackEvent(
            "CheckInSetupRemoved",
            mapOf(
                "eventType" to "esupervision.setup.removed",
                "crn" to PersonGenerator.DEFAULT_PERSON.crn,
                "eventNumber" to EventGenerator.EVENT_2.number,
                "checkInUrl" to null,
                "setupId" to "5b487c04-974d-44ca-b8c2-c95053d82479",
            )
        )
    }

    @Test
    fun `esupervision setup removed with ESPMP outcome code`() {
        val originalContact = contactRepository.findAll().single {
            it.externalReference == "urn:uk:gov:hmpps:esupervision:setup:a1b2c3d4-e5f6-7890-abcd-ef1234567890"
        }
        assertThat(originalContact.outcome?.code).isEqualTo(ContactOutcome.SETUP_COMPLETED)

        val notification = prepMessage(MessageGenerator.SETUP_REMOVED_MANUAL_STOP_A000001)
        channelManager.getChannel(queueName).publishAndWait(notification)

        val updatedContact = contactRepository.findAll().single { it.id == originalContact.id }
        assertThat(updatedContact.outcome?.code).isEqualTo(ContactOutcome.MANUAL_STOP)
        verify(telemetryService).trackEvent(
            "CheckInSetupRemoved",
            mapOf(
                "eventType" to "esupervision.setup.removed",
                "crn" to PersonGenerator.DEFAULT_PERSON.crn,
                "eventNumber" to EventGenerator.EVENT_2.number,
                "checkInUrl" to null,
                "setupId" to "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
            )
        )
    }

    @Test
    fun `esupervision setup removed with ESPNA outcome code`() {
        val originalContact = contactRepository.findAll().single {
            it.externalReference == "urn:uk:gov:hmpps:esupervision:setup:b2c3d4e5-f6a7-8901-bcde-f12345678901"
        }
        assertThat(originalContact.outcome?.code).isEqualTo(ContactOutcome.SETUP_COMPLETED)

        val notification = prepMessage(MessageGenerator.SETUP_REMOVED_NO_ACTIVE_EVENTS_A000001)
        channelManager.getChannel(queueName).publishAndWait(notification)

        val updatedContact = contactRepository.findAll().single { it.id == originalContact.id }
        assertThat(updatedContact.outcome?.code).isEqualTo(ContactOutcome.NO_ACTIVE_EVENTS)
        verify(telemetryService).trackEvent(
            "CheckInSetupRemoved",
            mapOf(
                "eventType" to "esupervision.setup.removed",
                "crn" to PersonGenerator.DEFAULT_PERSON.crn,
                "eventNumber" to EventGenerator.EVENT_2.number,
                "checkInUrl" to null,
                "setupId" to "b2c3d4e5-f6a7-8901-bcde-f12345678901",
            )
        )
    }

    @Test
    fun `esupervision setup removed with ESPRS outcome code`() {
        val originalContact = contactRepository.findAll().single {
            it.externalReference == "urn:uk:gov:hmpps:esupervision:setup:c3d4e5f6-a7b8-9012-cdef-123456789012"
        }
        assertThat(originalContact.outcome?.code).isEqualTo(ContactOutcome.SETUP_COMPLETED)

        val notification = prepMessage(MessageGenerator.SETUP_REMOVED_IN_RESET_A000001)
        channelManager.getChannel(queueName).publishAndWait(notification)

        val updatedContact = contactRepository.findAll().single { it.id == originalContact.id }
        assertThat(updatedContact.outcome?.code).isEqualTo(ContactOutcome.IN_RESET)
        verify(telemetryService).trackEvent(
            "CheckInSetupRemoved",
            mapOf(
                "eventType" to "esupervision.setup.removed",
                "crn" to PersonGenerator.DEFAULT_PERSON.crn,
                "eventNumber" to EventGenerator.EVENT_2.number,
                "checkInUrl" to null,
                "setupId" to "c3d4e5f6-a7b8-9012-cdef-123456789012",
            )
        )
    }

    @Test
    fun `sentence terminated updates setup contact using event number when setup id is missing`() {
        val originalContact = contactRepository.findAll()
            .single { it.person.id == PersonGenerator.SENTENCE_TERMINATED_PERSON.id }
        assertThat(originalContact.outcome?.code).isEqualTo(ContactOutcome.SETUP_COMPLETED)

        val notification = prepMessage(MessageGenerator.SENTENCE_TERMINATED_A000008)
        channelManager.getChannel(queueName).publishAndWait(notification)

        val updatedContact = contactRepository.findAll().single { it.id == originalContact.id }
        assertThat(updatedContact.outcome?.code).isEqualTo(ContactOutcome.SETUP_REMOVED)
        assertThat(updatedContact.externalReference).isEqualTo(originalContact.externalReference)
        assertThat(updatedContact.date).isEqualTo(originalContact.date)
        assertThat(updatedContact.startTime).isEqualTo(originalContact.startTime)
        verify(telemetryService).trackEvent(
            "CheckInSetupRemoved",
            mapOf(
                "eventType" to "probation-case.sentence.terminated",
                "crn" to PersonGenerator.SENTENCE_TERMINATED_PERSON.crn,
                "eventNumber" to EventGenerator.SENTENCE_TERMINATED_EVENT.number,
                "checkInUrl" to null,
                "setupId" to null,
            )
        )
    }

    @Test
    fun `esupervision update for missing CRN ignored`() {
        val notification = prepEvent("esupervision-updated-A000001", wireMockServer.port())
        notification.message.personReference.identifiers[0].set("value", "INVALID")

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).trackEvent(
            "CheckInEventIgnored",
            mapOf(
                "reason" to "CRN not found",
                "eventType" to "esupervision.check-in.updated",
                "crn" to "INVALID",
                "eventNumber" to EventGenerator.EVENT_2.number,
                "checkInUrl" to null,
                "setupId" to null,
            )
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["esupervision-updated-A000001", "esupervision-setup-removed-A000001"])
    fun `incorrect CRN is rejected`(file: String) {
        val notification = prepEvent(file, wireMockServer.port())
        notification.message.personReference.identifiers[0].set("value", "A000007")
        assertThatThrownBy { handler.handle(notification) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Case details mismatch")
    }

    @ParameterizedTest
    @ValueSource(strings = ["esupervision-updated-A000001", "esupervision-setup-removed-A000001"])
    fun `incorrect event number is rejected`(file: String) {
        val notification = prepEvent(file, wireMockServer.port()).run {
            copy(message = message.copy(additionalInformation = message.additionalInformation + mapOf("eventNumber" to "99")))
        }
        assertThatThrownBy { handler.handle(notification) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Case details mismatch")
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
        assertThat(contact.event?.id).isEqualTo(EventGenerator.EVENT_2.id)
        assertThat(contact.provider.id).isEqualTo(ProviderGenerator.DEFAULT_PROVIDER.id)
        assertThat(contact.team.id).isEqualTo(ProviderGenerator.DEFAULT_TEAM.id)
        assertThat(contact.staff.id).isEqualTo(ProviderGenerator.DEFAULT_STAFF.id)
        assertThat(contact.isSensitive).isEqualTo(false)
        assertThat(contact.notes).isEqualTo("Some notes about the expired check-in")
    }

    @Test
    fun `get contact details for a single crn`() {
        mockMvc.get("/case/${PersonGenerator.DEFAULT_PERSON.crn}") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "crn": "${PersonGenerator.DEFAULT_PERSON.crn}",
                          "name": {
                            "forename": "${PersonGenerator.DEFAULT_PERSON.firstName}",
                            "surname": "${PersonGenerator.DEFAULT_PERSON.lastName}"
                          },
                          "dateOfBirth": "${PersonGenerator.DEFAULT_PERSON.dateOfBirth}",
                          "mobile": "${PersonGenerator.DEFAULT_PERSON.mobile.toString()}",
                          "email": "${PersonGenerator.DEFAULT_PERSON.emailAddress}",
                          "events": [
                            {
                              "number": 2,
                              "mainOffence": {
                                "code": "03100",
                                "description": "Aggravated burglary in a building other than a dwelling (including attempts)"
                              },
                              "sentence": {
                                "date": "2026-03-01",
                                "description": "ORA Community Order (24 Months)"
                              }
                            },
                            {
                              "number": 3,
                              "mainOffence": {
                                "code": "03100",
                                "description": "Aggravated burglary in a building other than a dwelling (including attempts)"
                              }
                            }
                          ],
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
                          },
                          "contactSuspended": false
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
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `get multiple contact details`() {
        val crns = listOf(
            PersonGenerator.PERSON_CONTACT_DETAILS_1.crn,
            PersonGenerator.PERSON_CONTACT_DETAILS_2.crn,
        )
        mockMvc.post("/cases") {
            json = crns
            withToken()
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
        }
    }

    @Test
    fun `get multiple contact details only returns active events`() {
        mockMvc.post("/cases") {
            json = listOf(PersonGenerator.DEFAULT_PERSON.crn)
            withToken()
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].events.length()") { value(2) }
            jsonPath("$[0].events[0].number") { value(2) }
            jsonPath("$[0].events[1].number") { value(3) }
        }
    }

    @Test
    fun `get multiple contact details with one bad crn returns only existing crn details`() {
        val crns = listOf(
            PersonGenerator.PERSON_CONTACT_DETAILS_1.crn,
            "NOT_A_CRN",
        )
        mockMvc.post("/cases") {
            json = crns
            withToken()
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
        }
    }

    @Test
    fun `get contact details returns contactSuspended true when active contact suspended registration exists`() {
        mockMvc.get("/case/${PersonGenerator.PERSON_CONTACT_DETAILS_2.crn}") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("$.contactSuspended") { value(true) }
            }
    }

    @Test
    fun `get contact details returns contactSuspended false when contact suspended registration is deregistered`() {
        mockMvc.get("/case/${PersonGenerator.PERSON_CONTACT_DETAILS_1.crn}") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("$.contactSuspended") { value(false) }
            }
    }

    @Test
    fun `get multiple cases returns contactSuspended true for contact suspended registration case`() {
        mockMvc.post("/cases") {
            json = listOf(PersonGenerator.PERSON_CONTACT_DETAILS_2.crn)
            withToken()
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].contactSuspended") { value(true) }
        }
    }

    @Test
    fun `get cases with invalid crn returns valid but empty response`() {
        mockMvc.post("/cases") {
            json = listOf("Z999999")
            withToken()
        }.andExpect {
            status { isOk() }
            content { json("[]") }
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
        }.andExpect { status { isOk() } }
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
        }.andExpect { status { isOk() } }
    }

    @Test
    fun `incorrect punctuation is allowed`() {
        val testBody = PersonalDetails(
            crn = PersonGenerator.PUNCTUATION_IN_NAME.crn,
            name = uk.gov.justice.digital.hmpps.model.Name(
                forename = "Joe",
                surname = "OʼNeil" // note: using ʼ instead of '
            ),
            dateOfBirth = PersonGenerator.PUNCTUATION_IN_NAME.dateOfBirth
        )
        mockMvc.post("/case/{${PersonGenerator.PUNCTUATION_IN_NAME.crn}/validate-details") {
            json = testBody
            withToken()
        }.andExpect { status { isOk() } }
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
        }.andExpect { status { isBadRequest() } }
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
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `setup removal for contact with null event is logged and ignored`() {
        val notification = prepMessage(MessageGenerator.SETUP_REMOVED_NULL_EVENT_A000001)
        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).trackEvent(
            "CheckInEventIgnored",
            mapOf(
                "reason" to "Event not found for setup removal",
                "eventType" to "esupervision.setup.removed",
                "crn" to PersonGenerator.DEFAULT_PERSON.crn,
                "eventNumber" to EventGenerator.EVENT_2.number,
                "checkInUrl" to null,
                "setupId" to "d9e1f2a3-b4c5-6789-0abc-def123456789",
            )
        )
    }
}
