package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integration.delius.entity.AddressRepository
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.service.AddressService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@SpringBootTest
internal class MessagingIntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}") private val queueName: String,
    @Value("\${messaging.producer.topic}") private val topicName: String,
    private val channelManager: HmppsChannelManager,
    private val addressRepository: AddressRepository,
    private val jdbcTemplate: JdbcTemplate,
    private val wireMockServer: WireMockServer,
    private val addressService: AddressService,
    @MockitoBean private val telemetryService: TelemetryService,
) {
    @Test
    fun `creates address from domain event`() {
        publish("address-created")

        val address = addressRepository.findAllByPersonIdOrderByStartDateDesc(PersonGenerator.UPDATABLE_PERSON_ID)
            .single { it.postcode == "CR1 1AA" }

        assertThat(address.personId).isEqualTo(PersonGenerator.UPDATABLE_PERSON_ID)
        assertThat(address.addressNumber).isEqualTo("14")
        assertThat(address.buildingName).isEqualTo("Flat 2 Creation House")
        assertThat(address.streetName).isEqualTo("New street that is longer than 35 …")
        assertThat(address.streetName).hasSize(35)
        assertThat(address.district).isEqualTo("Created District")
        assertThat(address.townCity).isEqualTo("Created Town")
        assertThat(address.uprn).isEqualTo(100010001)
        assertThat(address.noFixedAbode).isFalse()
        assertThat(address.notes).isEqualTo("Created from CPR")
        assertThat(address.startDate!!.toInstant()).isEqualTo(Instant.parse("2026-02-03T09:15:30Z"))
        assertThat(address.status.code).isEqualTo("M")
        assertThat(address.type?.code).isEqualTo("A01C")
        assertThat(address.createdByUserId).isEqualTo(UserGenerator.AUDIT_USER.id)
        assertThat(address.createdDatetime).isCloseTo(ZonedDateTime.now(), within(5, ChronoUnit.SECONDS))
        assertThat(address.lastUpdatedUserId).isEqualTo(UserGenerator.AUDIT_USER.id)
        assertThat(address.lastUpdatedDatetime).isCloseTo(ZonedDateTime.now(), within(5, ChronoUnit.SECONDS))
        verify(telemetryService).trackEvent(
            "AddressCreated",
            mapOf(
                "crn" to "U123456",
                "cprAddressId" to "cpr-address-created",
                "deliusAddressId" to address.id.toString(),
                "addressStatus" to address.status.code,
                "addressType" to address.type?.code,
                "startDate" to address.startDate?.toString(),
                "endDate" to address.endDate?.toString(),
            )
        )
        assertAddressDomainEvent(
            eventType = "probation-case.address.created",
            description = "A new address has been created on the probation case",
            cprAddressId = "cpr-address-created",
            deliusAddressId = address.id!!,
            addressStatus = "M"
        )
    }

    @Test
    fun `updates address from domain event`() {
        val addressId = PersonGenerator.UPDATABLE_PERSON_ADDRESSES[0].id!!
        val addressBefore = addressRepository.findByIdOrNull(addressId)!!
        publish("address-updated")

        val address = addressRepository.findByIdOrNull(addressId)!!
        assertThat(address.addressNumber).isEqualTo("22")
        assertThat(address.buildingName).isEqualTo("Flat 4 Updated House")
        assertThat(address.streetName).isEqualTo("Updated Street")
        assertThat(address.district).isEqualTo("Updated District longer than 35 ch…")
        assertThat(address.district).hasSize(35)
        assertThat(address.townCity).isEqualTo("Updated Town")
        assertThat(address.postcode).isEqualTo("UP2 2BB")
        assertThat(address.uprn).isEqualTo(100020002)
        assertThat(address.telephoneNumber).isEqualTo("020 0000 0001")
        assertThat(address.notes).isEqualTo("Existing update notes\n\nUpdated from CPR")
        assertThat(address.startDate!!.toInstant()).isEqualTo(Instant.parse("2026-03-04T10:15:30Z"))
        assertThat(address.endDate!!.toInstant()).isEqualTo(Instant.parse("2026-04-05T11:45:00Z"))
        assertThat(address.status.code).isEqualTo("P")
        assertThat(address.type?.code).isEqualTo("A01C")
        assertThat(address.createdByUserId).isEqualTo(UserGenerator.AUDIT_USER.id)
        assertThat(address.createdDatetime).isEqualTo(addressBefore.createdDatetime)
        assertThat(address.lastUpdatedUserId).isEqualTo(UserGenerator.AUDIT_USER.id)
        assertThat(address.lastUpdatedDatetime)
            .isNotEqualTo(addressBefore.lastUpdatedDatetime)
            .isCloseTo(ZonedDateTime.now(), within(5, ChronoUnit.SECONDS))
        verify(telemetryService).trackEvent(
            "AddressUpdated",
            mapOf(
                "crn" to "U123456",
                "cprAddressId" to "cpr-address-updated",
                "deliusAddressId" to addressId.toString(),
                "addressStatus" to address.status.code,
                "addressType" to address.type?.code,
                "startDate" to address.startDate?.toString(),
                "endDate" to address.endDate?.toString(),
            )
        )
        assertAddressDomainEvent(
            eventType = "probation-case.address.updated",
            description = "An address has been updated on the probation case",
            cprAddressId = "cpr-address-updated",
            deliusAddressId = addressId,
            addressStatus = "P"
        )
    }

    @Test
    fun `updates address when optional values are absent`() {
        val addressId = createForUpdate(90000101)
        stubAddress(
            "cpr-address-optional-values-absent",
            // language=json
            """
            {
              "cprAddressId": "cpr-address-optional-values-absent",
              "postcode": "AB1 2CD",
              "status": { "code": "M" },
              "usages": []
            }
            """.trimIndent()
        )

        addressService.updateAddress("U123456", "cpr-address-optional-values-absent", addressId)

        val address = addressRepository.findByIdOrNull(addressId)!!
        assertThat(address.uprn).isNull()
        assertThat(address.noFixedAbode).isFalse()
        assertThat(address.notes).isNull()
        assertThat(address.type).isNull()
    }

    @Test
    fun `updates address with invalid uprn no fixed abode and inactive usage`() {
        val addressId = createForUpdate(90000102, notes = "Existing notes only")
        stubAddress(
            "cpr-address-inactive-usage",
            // language=json
            """
            {
              "cprAddressId": "cpr-address-inactive-usage",
              "noFixedAbode": true,
              "uprn": "not-a-number",
              "postcode": "NF1 1AB",
              "status": { "code": "M" },
              "usages": [
                { "code": "A01C", "description": "Rental accommodation - private rental", "isActive": false }
              ]
            }
            """.trimIndent()
        )

        addressService.updateAddress("U123456", "cpr-address-inactive-usage", addressId)

        val address = addressRepository.findByIdOrNull(addressId)!!
        assertThat(address.uprn).isNull()
        assertThat(address.noFixedAbode).isTrue()
        assertThat(address.notes).isEqualTo("Existing notes only")
        assertThat(address.type).isNull()
    }

    @Test
    fun `updates address when active usage has no code`() {
        val addressId = createForUpdate(90000103)
        stubAddress(
            "cpr-address-usage-without-code",
            // language=json
            """
            {
              "cprAddressId": "cpr-address-usage-without-code",
              "comment": "CPR comment only",
              "postcode": "NC1 1AB",
              "status": { "code": "M" },
              "usages": [
                { "description": "Usage without a code", "isActive": true }
              ]
            }
            """.trimIndent()
        )

        addressService.updateAddress("U123456", "cpr-address-usage-without-code", addressId)

        val address = addressRepository.findByIdOrNull(addressId)!!
        assertThat(address.notes).isEqualTo("CPR comment only")
        assertThat(address.type).isNull()
    }

    @Test
    fun `update fails when address has multiple active usages`() {
        val addressId = createForUpdate(90000104)
        stubAddress(
            "cpr-address-multiple-active-usages",
            // language=json
            """
            {
              "cprAddressId": "cpr-address-multiple-active-usages",
              "status": { "code": "M" },
              "usages": [
                { "code": "A01C", "description": "Rental accommodation - private rental", "isActive": true },
                { "code": "A01C", "description": "Rental accommodation - private rental", "isActive": true }
              ]
            }
            """.trimIndent()
        )

        assertThatThrownBy { addressService.updateAddress("U123456", "cpr-address-multiple-active-usages", addressId) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Cannot handle multiple address types")
    }

    @Test
    fun `hard deletes address from domain event`() {
        val addressId = PersonGenerator.UPDATABLE_PERSON_ADDRESSES[1].id!!
        val existingAddress = addressRepository.findByIdOrNull(addressId)!!
        assertThat(existingAddress.softDeleted).isFalse

        publish("address-deleted")

        assertThat(addressRepository.findByIdOrNull(addressId)).isNull()
        assertThat(count(addressId)).isEqualTo(0)
        verify(telemetryService).trackEvent(
            "AddressDeleted",
            mapOf(
                "crn" to "U123456",
                "cprAddressId" to "cpr-address-deleted",
                "deliusAddressId" to addressId.toString(),
                "addressStatus" to existingAddress.status.code,
                "addressType" to existingAddress.type?.code,
                "startDate" to existingAddress.startDate?.toString(),
                "endDate" to existingAddress.endDate?.toString(),
            )
        )
        assertAddressDomainEvent(
            eventType = "probation-case.address.deleted",
            description = "An address has been deleted from the probation case",
            cprAddressId = "cpr-address-deleted",
            deliusAddressId = addressId,
            addressStatus = "M"
        )
    }

    @Test
    fun `update fails when Delius address is not found`() {
        stubAddress(
            "cpr-address-missing-existing",
            // language=json
            """
            {
              "cprAddressId": "cpr-address-missing-existing",
              "deliusAddressId": 99999999,
              "status": { "code": "M" }
            }
            """.trimIndent()
        )

        assertThatThrownBy { addressService.updateAddress("U123456", "cpr-address-missing-existing", 99999999) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("PersonAddress with id of 99999999 not found")
    }

    @Test
    fun `create fails when address status is missing`() {
        stubAddress(
            "cpr-address-missing-status",
            // language=json
            """
            {
              "cprAddressId": "cpr-address-missing-status",
              "status": {},
              "usages": [{ "code": "A01C", "isActive": true }]
            }
            """.trimIndent()
        )

        assertThatThrownBy { addressService.createAddress("U123456", "cpr-address-missing-status") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Address status is required")
    }

    @Test
    fun `create fails when address type is missing from reference data`() {
        stubAddress(
            "cpr-address-missing-type",
            // language=json
            """
            {
              "cprAddressId": "cpr-address-missing-type",
              "status": { "code": "M" },
              "usages": [{ "code": "MISSING", "isActive": true }]
            }
            """.trimIndent()
        )

        assertThatThrownBy { addressService.createAddress("U123456", "cpr-address-missing-type") }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("ADDRESS TYPE with code of MISSING not found")
    }

    private fun publish(fileName: String) =
        channelManager.getChannel(queueName).publishAndWait(prepEvent(fileName, wireMockServer.port()))

    private fun assertAddressDomainEvent(
        eventType: String,
        description: String,
        cprAddressId: String,
        deliusAddressId: Long,
        addressStatus: String
    ) {
        val topic = channelManager.getChannel(topicName)
        val notification = checkNotNull(topic.receive()) { "Expected address domain event to be published" }
        topic.done(notification.id)
        val domainEvent = notification.message as HmppsDomainEvent

        assertThat(notification.eventType).isEqualTo(eventType)
        assertThat(notification.attributes["eventSource"]?.value).isEqualTo("core-person-record")
        assertThat(domainEvent.version).isEqualTo(1)
        assertThat(domainEvent.eventType).isEqualTo(eventType)
        assertThat(domainEvent.description).isEqualTo(description)
        assertThat(domainEvent.personReference.findCrn()).isEqualTo("U123456")
        assertThat(domainEvent.additionalInformation).containsExactlyInAnyOrderEntriesOf(
            mapOf(
                "addressStatus" to addressStatus,
                "addressId" to deliusAddressId,
                "corePersonAddressId" to cprAddressId,
            )
        )
        assertThat(topic.receive()).isNull()
    }

    private fun stubAddress(id: String, json: String) {
        wireMockServer.stubFor(get("/core-person-record/person/probation/U123456/address/$id").willReturn(okJson(json)))
    }

    private fun createForUpdate(id: Long, notes: String? = null) = addressRepository.save(
        PersonGenerator.generateAddress(
            id = id,
            personId = PersonGenerator.UPDATABLE_PERSON_ID,
            status = PersonGenerator.MAIN_ADDRESS,
            type = PersonGenerator.PRIVATE_RENTAL,
            notes = notes,
            startDate = LocalDate.of(2026, 1, 10)
        )
    ).id!!

    private fun count(addressId: Long) = jdbcTemplate.queryForObject<Int>(
        "select count(*) from offender_address where offender_address_id = ?",
        addressId
    )
}
