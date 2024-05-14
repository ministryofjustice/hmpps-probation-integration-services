package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import software.amazon.awssdk.utils.ImmutableMap
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PARTITION_AREA
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PREVIOUS_CONVICTION_DOC
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DISABILITY_TYPE_1
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.PROVISION_TYPE_1
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RELIGION
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class OffenderIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `API call retuns probation record with active sentence`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        val detailResponse = mockMvc
            .perform(get("/probation-case/$crn").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<OffenderDetailSummary>()
        assertThat(detailResponse.preferredName, equalTo("Other name"))
        assertThat(detailResponse.softDeleted, equalTo(false))
        assertThat(detailResponse.activeProbationManagedSentence, equalTo(true))
        assertThat(
            detailResponse.contactDetails.phoneNumbers, equalTo(
                listOf(
                    PhoneNumber(
                        PersonGenerator.CURRENTLY_MANAGED.telephoneNumber,
                        PhoneTypes.TELEPHONE.name
                    ), PhoneNumber(
                        PersonGenerator.CURRENTLY_MANAGED.mobileNumber,
                        PhoneTypes.MOBILE.name
                    )
                )
            )
        )
        assertThat(detailResponse.contactDetails.allowSMS, equalTo(true))
        assertThat(detailResponse.contactDetails.emailAddresses, equalTo(listOf("test@test.none")))
        assertThat(detailResponse.currentDisposal, equalTo("1"))
        assertThat(detailResponse.currentExclusion, equalTo(false))
        assertThat(detailResponse.currentRestriction, equalTo(false))
        assertThat(detailResponse.dateOfBirth, equalTo(LocalDate.of(1977, 8, 12)))
        assertThat(detailResponse.firstName, equalTo("TestForename"))
        assertThat(detailResponse.middleNames, equalTo(listOf("MiddleName", "OtherMiddleName")))
        assertThat(detailResponse.offenderId, equalTo(PersonGenerator.CURRENTLY_MANAGED.id))
        assertThat(detailResponse.offenderProfile.genderIdentity, equalTo("Some gender identity"))
        assertThat(
            detailResponse.offenderProfile.selfDescribedGenderIdentity,
            equalTo("Some self described gender identity")
        )
        assertThat(
            detailResponse.offenderProfile.selfDescribedGenderIdentity,
            equalTo("Some self described gender identity")
        )
        assertThat(
            detailResponse.offenderProfile.disabilities[0].disabilityType.description,
            equalTo(DISABILITY_TYPE_1.description)
        )
        assertThat(detailResponse.offenderProfile.ethnicity, equalTo("Some ethnicity"))
        assertThat(detailResponse.offenderProfile.immigrationStatus, equalTo("Some immigration status"))
        assertThat(detailResponse.offenderProfile.nationality, equalTo("British"))
        assertThat(detailResponse.offenderProfile.offenderDetails, equalTo("Some details"))
        assertThat(
            detailResponse.offenderProfile.offenderLanguages, equalTo(
                OffenderLanguages(
                    languageConcerns = "A concern",
                    primaryLanguage = "English",
                    requiresInterpreter = false
                )
            )
        )
        assertThat(
            detailResponse.offenderProfile.previousConviction, equalTo(
                PreviousConviction(
                    convictionDate = PREVIOUS_CONVICTION_DOC.createdAt.toLocalDate(),
                    detail = ImmutableMap.of("documentName", PREVIOUS_CONVICTION_DOC.name)
                )
            )
        )
        assertThat(
            detailResponse.offenderProfile.provisions[0].provisionType.description,
            equalTo(PROVISION_TYPE_1.description)
        )
        assertThat(detailResponse.offenderProfile.religion, equalTo(RELIGION.description))
        assertThat(detailResponse.offenderProfile.remandStatus, equalTo("Remand Status"))
        assertThat(detailResponse.offenderProfile.riskColour, equalTo("RED"))
        assertThat(detailResponse.offenderProfile.secondaryNationality, equalTo("French"))
        assertThat(detailResponse.offenderProfile.sexualOrientation, equalTo("A sexual orientation"))
        assertThat(detailResponse.otherIds.crn, equalTo(crn))
        assertThat(detailResponse.otherIds.niNumber, equalTo("JK002213K"))
        assertThat(detailResponse.otherIds.pncNumber, equalTo("1234567890123"))
        assertThat(detailResponse.otherIds.nomsNumber, equalTo("NOMS123"))
        assertThat(detailResponse.otherIds.croNumber, equalTo("CRO123"))
        assertThat(detailResponse.otherIds.immigrationNumber, equalTo("IMA123"))
        assertThat(detailResponse.otherIds.mostRecentPrisonerNumber, equalTo("PRS123"))
        assertThat(detailResponse.partitionArea, equalTo(PARTITION_AREA.area))
        assertThat(detailResponse.previousSurname, equalTo("Previous"))
        assertThat(detailResponse.surname, equalTo("TestSurname"))
    }

    @Test
    fun `API call probation record not found`() {
        mockMvc
            .perform(get("/probation-case/A123456").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `API call retuns probation record with no active sentence`() {
        val crn = PersonGenerator.NO_SENTENCE.crn
        val detailResponse = mockMvc
            .perform(get("/probation-case/$crn").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<OffenderDetailSummary>()
        assertThat(detailResponse.currentDisposal, equalTo("0"))
        assertThat(detailResponse.activeProbationManagedSentence, equalTo(false))
    }
}
