package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.MergeAppointment
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.CompletableFuture

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class MergeAppointmentIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var contactRepository: ContactRepository

    private fun makeRequest(person: Person, referralId: UUID, request: MergeAppointment, result: ResultMatcher) {
        val json = objectMapper.readTree(objectMapper.writeValueAsString(request))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/probation-case/${person.crn}/referrals/$referralId/appointments")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toPrettyString())
        ).andExpect(result)
    }

    @Test
    @Order(1)
    fun `creates appointment with external reference only once`() {
        val person = PersonGenerator.NO_APPOINTMENTS
        val referralId = UUID.fromString("09c62549-bcd3-49a9-8120-7811b76925e5")
        val start = ZonedDateTime.now().plusDays(14)
        val end = start.plusMinutes(30)
        val mergeAppointment = MergeAppointment(
            UUID.randomUUID(),
            referralId,
            "RE1234F",
            start,
            end,
            "Appointment Notes",
            "DEFAULT",
            false,
            null
        )
        val result = MockMvcResultMatchers.status().isNoContent

        val r1 = CompletableFuture.supplyAsync {
            makeRequest(person, referralId, mergeAppointment, result)
        }
        val r2 = CompletableFuture.supplyAsync {
            makeRequest(person, referralId, mergeAppointment, result)
        }

        CompletableFuture.allOf(r1, r2).join()

        val appointment = assertDoesNotThrow {
            contactRepository.findByPersonCrnAndExternalReference(
                person.crn,
                mergeAppointment.urn
            )
        }
        Assertions.assertNotNull(appointment)
        assertThat(appointment!!.date, equalTo(mergeAppointment.start.toLocalDate()))
        assertThat(appointment.startTime, isCloseTo(mergeAppointment.start))
        assertThat(appointment.endTime!!, isCloseTo(mergeAppointment.end))
    }

    @Test
    @Order(2)
    fun `cannot save appointment that conflicts with existing appointment`() {
        val referralId = UUID.fromString("09c62549-bcd3-49a9-8120-7811b76925e5")
        val start = ZonedDateTime.now().plusDays(14).plusMinutes(10)
        val end = start.plusMinutes(30)
        val person = PersonGenerator.NO_APPOINTMENTS
        val mergeAppointment = MergeAppointment(
            UUID.randomUUID(),
            referralId,
            "RE1234F",
            start,
            end,
            "Appointment Notes",
            "DEFAULT",
            false,
            null
        )

        makeRequest(person, referralId, mergeAppointment, MockMvcResultMatchers.status().isConflict)
    }

    @Test
    @Order(3)
    fun `cannot save past appointment without an outcome`() {
        val person = PersonGenerator.NO_APPOINTMENTS
        val referralId = UUID.fromString("09c62549-bcd3-49a9-8120-7811b76925e5")
        val start = ZonedDateTime.now().minusDays(1)
        val end = start.plusMinutes(30)
        val mergeAppointment = MergeAppointment(
            UUID.randomUUID(),
            referralId,
            "RE1234F",
            start,
            end,
            "Appointment Notes",
            "DEFAULT",
            false,
            null
        )

        makeRequest(person, referralId, mergeAppointment, MockMvcResultMatchers.status().isBadRequest)
    }
}
