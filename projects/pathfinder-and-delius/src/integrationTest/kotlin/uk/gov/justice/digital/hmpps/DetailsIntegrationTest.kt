package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.ConvictionEventGenerator
import uk.gov.justice.digital.hmpps.data.generator.DetailsGenerator
import uk.gov.justice.digital.hmpps.data.generator.KeyDateGenerator
import uk.gov.justice.digital.hmpps.data.generator.NSIGenerator
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class DetailsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `API call retuns a success response using NOMS`() {
        val noms = DetailsGenerator.PERSON.nomsNumber
        mockMvc
            .perform(get("/detail/$noms?type=NOMS").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(getDetail())
    }

    @Test
    fun `API call retuns a success response using CRN`() {
        val crn = DetailsGenerator.PERSON.crn
        mockMvc
            .perform(get("/detail/$crn?type=CRN").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(getDetail())
    }

    private fun getDetail(): Detail = Detail(
        DetailsGenerator.PERSON.name(),
        DetailsGenerator.PERSON.dateOfBirth,
        DetailsGenerator.PERSON.crn,
        DetailsGenerator.PERSON.nomsNumber,
        DetailsGenerator.PERSON.pncNumber,
        Manager(
            name = Name(
                DetailsGenerator.STAFF.forename,
                DetailsGenerator.STAFF.middleName,
                DetailsGenerator.STAFF.surname
            ),
            team = Team(
                code = DetailsGenerator.TEAM.code,
                localDeliveryUnit = Ldu(
                    code = DetailsGenerator.DISTRICT.code,
                    name = DetailsGenerator.DISTRICT.description
                )
            ),
            provider = Provider(
                code = DetailsGenerator.DEFAULT_PA.code,
                description = DetailsGenerator.DEFAULT_PA.description
            )
        ),
        true,
        true,
        ConvictionEventGenerator.OFFENCE_MAIN_TYPE.description,
        Profile(DetailsGenerator.PERSON.nationality?.description, DetailsGenerator.PERSON.religion?.description),
        listOf(
            KeyDate(
                KeyDateGenerator.SED_KEYDATE.code,
                KeyDateGenerator.SED_KEYDATE.description,
                KeyDateGenerator.KEYDATE.date
            )
        ),
        DetailsGenerator.RELEASE.date,
        DetailsGenerator.RELEASE.releaseType.description,
        DetailsGenerator.INSTITUTION.name,
        DetailsGenerator.RECALL.date,
        DetailsGenerator.RECALL.reason.description,
        NSIGenerator.RECALL_NSI.referralDate,
        NSIGenerator.BREACH_NSI.referralDate,
        listOf(
            OffenderAlias(
                id = DetailsGenerator.ALIAS_1.aliasID,
                dateOfBirth = DetailsGenerator.ALIAS_1.dateOfBirth,
                firstName = DetailsGenerator.ALIAS_1.firstName,
                middleNames = listOf(DetailsGenerator.ALIAS_1.secondName!!, DetailsGenerator.ALIAS_1.thirdName!!),
                surname = DetailsGenerator.ALIAS_1.surname,
                gender = DetailsGenerator.MALE.description
            ),
            OffenderAlias(
                id = DetailsGenerator.ALIAS_2.aliasID,
                dateOfBirth = DetailsGenerator.ALIAS_2.dateOfBirth,
                firstName = DetailsGenerator.ALIAS_2.firstName,
                middleNames = listOf(DetailsGenerator.ALIAS_2.secondName!!, DetailsGenerator.ALIAS_2.thirdName!!),
                surname = DetailsGenerator.ALIAS_2.surname,
                gender = DetailsGenerator.FEMALE.description
            )
        )
    )
}
