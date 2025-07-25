package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.appointment.AppointmentType
import uk.gov.justice.digital.hmpps.api.model.appointment.AppointmentTypeResponse
import uk.gov.justice.digital.hmpps.api.model.appointment.ContactTypeAssociation
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.api.model.appointment.MinimalNsi
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.api.model.user.Team
import uk.gov.justice.digital.hmpps.api.model.user.TeamResponse
import uk.gov.justice.digital.hmpps.data.generator.AppointmentGenerator.APPOINTMENT_TYPES
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.APPT_CT_3
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LC_WITHOUT_NOTES
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LC_WITH_1500_CHAR_NOTE
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LC_WITH_NOTES
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LC_WITH_NOTES_WITHOUT_ADDED_BY
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LIC_COND_MAIN_CAT
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.DEFAULT_LOCATION
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.ACTIVE_ORDER
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.EVENT_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.EVENT_2
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.REQUIREMENT
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.REQUIREMENT_UNPAID_WORK
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.service.toAppointmentType
import uk.gov.justice.digital.hmpps.service.toLocationDetails
import uk.gov.justice.digital.hmpps.service.toSummary
import uk.gov.justice.digital.hmpps.service.toUser
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AppointmentIntegrationTest {
    @Autowired
    internal lateinit var mockMvc: MockMvc

    @ParameterizedTest
    @ValueSource(strings = ["D123456/contact-type/abc", "types"])
    fun `unauthorized status returned`(path: String) {
        mockMvc
            .perform(get("/appointment/$path"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `no person records associated with contact type`() {
        val code = CreateAppointment.Type.PlannedDoorstepContactNS.code
        val expected = ContactTypeAssociation(
            PersonDetailsGenerator.PERSONAL_DETAILS.toSummary(),
            code,
            true
        )
        val response = mockMvc
            .perform(get("/appointment/${PersonDetailsGenerator.PERSONAL_DETAILS.crn}/contact-type/${code}").withToken())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<ContactTypeAssociation>()

        assertEquals(expected, response)
    }

    @Test
    fun `person records associated with contact type`() {
        val code = CreateAppointment.Type.HomeVisitToCaseNS.code
        val expected = ContactTypeAssociation(
            personSummary = PersonGenerator.OVERVIEW.toSummary(),
            contactTypeCode = code,
            associatedWithPerson = false,
            emptyList(),
            listOf(
                MinimalSentence(EVENT_2.id, EVENT_2.eventNumber, MinimalOrder("Pre-Sentence")),
                MinimalSentence(
                    id = EVENT_1.id,
                    eventNumber = EVENT_1.eventNumber,
                    order = MinimalOrder(ACTIVE_ORDER.type.description + " (12 Months)", ACTIVE_ORDER.date),
                    nsis = listOf(
                        MinimalNsi(PersonGenerator.BREACH_ON_ACTIVE_ORDER.id, "BRE description"),
                        MinimalNsi(PersonGenerator.OPD_NSI.id, "OPD1 description (OPD1 subtype)")
                    ),
                    licenceConditions = listOf(
                        MinimalLicenceCondition(LC_WITHOUT_NOTES.id, LIC_COND_MAIN_CAT.description),
                        MinimalLicenceCondition(LC_WITH_NOTES.id, LIC_COND_MAIN_CAT.description),
                        MinimalLicenceCondition(LC_WITH_NOTES_WITHOUT_ADDED_BY.id, LIC_COND_MAIN_CAT.description),
                        MinimalLicenceCondition(LC_WITH_1500_CHAR_NOTE.id, LIC_COND_MAIN_CAT.description)
                    ),
                    requirements = listOf(
                        MinimalRequirement(REQUIREMENT.id, "2 of 12 RAR days completed"),
                        MinimalRequirement(REQUIREMENT_UNPAID_WORK.id, "Unpaid Work - Intensive")
                    )
                )
            )
        )
        val response = mockMvc
            .perform(get("/appointment/${PersonGenerator.OVERVIEW.crn}/contact-type/${code}").withToken())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<ContactTypeAssociation>()

        assertEquals(expected, response)
    }

    @Test
    fun `return mpop contact types`() {
        val response = mockMvc
            .perform(get("/appointment/types").withToken())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<AppointmentTypeResponse>()

        val types = mutableListOf<AppointmentType>()
        types.addAll(APPOINTMENT_TYPES.map { it.toAppointmentType() })
        types.add(5, APPT_CT_3.toAppointmentType())

        val expected =
            AppointmentTypeResponse(types)
        assertEquals(expected, response)
    }

    @Test
    fun `return teams by provider`() {
        val response = mockMvc
            .perform(get("/appointment/teams/provider/${DEFAULT_PROVIDER.code}").withToken())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<TeamResponse>()

        val expected = TeamResponse(
            listOf(
                Team(code = DEFAULT_TEAM.code, description = DEFAULT_TEAM.description),
                Team(code = OffenderManagerGenerator.TEAM.code, description = OffenderManagerGenerator.TEAM.description)
            )
        )
        assertEquals(expected, response)
    }

    @Test
    fun `return location by provider and team`() {
        val response = mockMvc
            .perform(
                get("/appointment/location/provider/${DEFAULT_PROVIDER.code}/team/${OffenderManagerGenerator.TEAM.code}")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<ProviderOfficeLocation>()

        val expected = ProviderOfficeLocation(listOf(DEFAULT_LOCATION.toLocationDetails()))

        assertEquals(expected, response)
    }

    @Test
    fun `get staff by team`() {
        val response = mockMvc
            .perform(
                get("/appointment/staff/team/${OffenderManagerGenerator.TEAM.code}")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<StaffTeam>()

        val expected = StaffTeam(
            listOf(
                OffenderManagerGenerator.STAFF_USER_1.toUser(),
                unallocatedUser,
                OffenderManagerGenerator.STAFF_USER_2.toUser(),
            )
        )
        assertEquals(expected, response)
    }
}