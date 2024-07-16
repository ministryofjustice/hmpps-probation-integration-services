package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.isEqualTo
import software.amazon.awssdk.utils.ImmutableMap
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.AreaGenerator.PARTITION_AREA
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.ADDRESS
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PREVIOUS_CONVICTION_DOC
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DEFAULT_ALLOCATION_REASON
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DISABILITY_TYPE_1
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.PROVISION_TYPE_1
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RELIGION
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator.ALLOCATED
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class OffenderIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `Summary API call retuns probation record with active sentence`() {
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
    fun `Summary API call probation record not found`() {
        mockMvc
            .perform(get("/probation-case/A123456").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Summary API call retuns probation record with no active sentence`() {
        val crn = PersonGenerator.NO_SENTENCE.crn
        val detailResponse = mockMvc
            .perform(get("/probation-case/$crn").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<OffenderDetailSummary>()
        assertThat(detailResponse.currentDisposal, equalTo("0"))
        assertThat(detailResponse.activeProbationManagedSentence, equalTo(false))
    }

    @Test
    fun `Detail API call retuns probation record with active sentence`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        val detailResponse = mockMvc
            .perform(get("/probation-case/$crn/all").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<OffenderDetail>()
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
        assertThat(detailResponse.contactDetails.addresses[0].addressNumber, equalTo(ADDRESS.addressNumber))
        assertThat(detailResponse.contactDetails.addresses[0].county, equalTo(ADDRESS.county))
        assertThat(detailResponse.contactDetails.addresses[0].status, equalTo(ADDRESS.status.keyValueOf()))
        assertThat(detailResponse.contactDetails.allowSMS, equalTo(true))
        assertThat(detailResponse.contactDetails.emailAddresses, equalTo(listOf("test@test.none")))
        assertThat(detailResponse.currentDisposal, equalTo("1"))
        assertThat(detailResponse.currentExclusion, equalTo(false))
        assertThat(detailResponse.currentRestriction, equalTo(false))
        assertThat(detailResponse.exclusionMessage, equalTo("exclusionMessage"))
        assertThat(detailResponse.restrictionMessage, equalTo("restrictionMessage"))
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

        assertThat(detailResponse.offenderAliases[0].dateOfBirth, equalTo(LocalDate.of(1968, 1, 1)))
        assertThat(detailResponse.offenderAliases[0].firstName, equalTo("Bob"))
        assertThat(detailResponse.offenderAliases[0].middleNames, equalTo(listOf("Reg", "Xavier")))

        assertThat(
            detailResponse.offenderManagers[0].providerEmployee,
            equalTo(Human("ProvEmpForename1 ProvEmpForename2", "ProvEmpSurname"))
        )
        assertThat(detailResponse.offenderManagers[0].trustOfficer, equalTo(Human("Off1 Off2", "OffSurname")))
        assertThat(
            detailResponse.offenderManagers[0].probationArea.description,
            equalTo(ProviderGenerator.DEFAULT.description)
        )
        assertThat(
            detailResponse.offenderManagers[0].staff,
            equalTo(StaffHuman(ALLOCATED.code, ALLOCATED.forename, ALLOCATED.surname, false))
        )
        assertThat(detailResponse.offenderManagers[0].allocationReason, equalTo(DEFAULT_ALLOCATION_REASON.keyValueOf()))
        assertThat(detailResponse.offenderManagers[0].partitionArea, equalTo(PARTITION_AREA.area))
        assertThat(detailResponse.offenderManagers[0].team!!.code.trim(), equalTo(TeamGenerator.DEFAULT.code.trim()))
        assertThat(detailResponse.offenderManagers[0].team!!.description, equalTo(TeamGenerator.DEFAULT.description))
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
        assertThat(detailResponse.currentTier, equalTo("B2"))
        assertThat(detailResponse.previousSurname, equalTo("Previous"))
        assertThat(detailResponse.surname, equalTo("TestSurname"))
    }

    @Test
    fun `Detail API call probation record not found`() {
        mockMvc
            .perform(get("/probation-case/A123456/all").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Detail API call retuns probation record with no active sentence`() {
        val crn = PersonGenerator.NO_SENTENCE.crn
        val detailResponse = mockMvc
            .perform(get("/probation-case/$crn/all").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<OffenderDetailSummary>()
        assertThat(detailResponse.currentDisposal, equalTo("0"))
        assertThat(detailResponse.activeProbationManagedSentence, equalTo(false))
    }

    @Test
    fun `All offender managers API call not including teams retuns successfully`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        val allOffenderManagers = mockMvc
            .perform(get("/probation-case/$crn/allOffenderManagers").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<List<CommunityOrPrisonOffenderManager>>()
        assertThat(allOffenderManagers[0].staff?.surname, equalTo(StaffGenerator.ALLOCATED.surname))
        assertThat(
            allOffenderManagers[0].probationArea?.institution?.institutionName,
            equalTo(ProviderGenerator.DEFAULT.institution?.institutionName)
        )
        assertThat(allOffenderManagers[0].probationArea?.teams?.size, equalTo(0))
    }

    @Test
    fun `All offender managers API call including teams retuns successfully`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        val allOffenderManagers = mockMvc
            .perform(get("/probation-case/$crn/allOffenderManagers?includeProbationAreaTeams=true").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<List<CommunityOrPrisonOffenderManager>>()
        assertThat(allOffenderManagers[0].staff?.surname, equalTo(StaffGenerator.ALLOCATED.surname))
        assertThat(
            allOffenderManagers[0].probationArea?.institution?.institutionName,
            equalTo(ProviderGenerator.DEFAULT.institution?.institutionName)
        )
        assertThat(allOffenderManagers[0].probationArea?.teams?.size, equalTo(2))
        assertThat(allOffenderManagers[0].isPrisonOffenderManager, equalTo(false))
        assertThat(allOffenderManagers[1].isPrisonOffenderManager, equalTo(true))
        assertThat(
            allOffenderManagers[1].probationArea?.teams?.get(1)?.externalProvider?.description,
            equalTo(ProviderTeamGenerator.EXTERNAL_PROVIDER.description)
        )
    }

    @Test
    fun `All offender managers crn not found`() {
        mockMvc
            .perform(get("/probation-case/X999999/allOffenderManagers").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Detail API call probation excluded case`() {
        val resp = mockMvc
            .perform(get("/probation-case/${PersonGenerator.EXCLUDED_CASE.crn}/all").withToken())
            .andExpect(status().isEqualTo(HttpStatus.FORBIDDEN.value()))
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(resp.message, equalTo(PersonGenerator.EXCLUDED_CASE.exclusionMessage))
    }

    @Test
    fun `Detail API call probation restricted case returns 200`() {
        val resp = mockMvc
            .perform(get("/probation-case/${PersonGenerator.RESTRICTED_CASE.crn}/all").withToken())
            .andExpect(status().isOk)
    }

    @Test
    fun `Summary API call probation excluded case`() {
        val resp = mockMvc
            .perform(get("/probation-case/${PersonGenerator.EXCLUDED_CASE.crn}").withToken())
            .andExpect(status().isEqualTo(HttpStatus.FORBIDDEN.value()))
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(resp.message, equalTo(PersonGenerator.EXCLUDED_CASE.exclusionMessage))
    }

    @Test
    fun `Summary API call probation restricted case returns 200`() {
        val resp = mockMvc
            .perform(get("/probation-case/${PersonGenerator.RESTRICTED_CASE.crn}").withToken())
            .andExpect(status().isOk)
    }
}
