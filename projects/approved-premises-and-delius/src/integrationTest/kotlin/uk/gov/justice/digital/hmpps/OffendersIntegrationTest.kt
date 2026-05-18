package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.OffenderAliasGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.model.OffenderDetail
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.time.LocalDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class OffendersIntegrationTest(
    @Autowired private val mockMvc: MockMvc
) {
    @Test
    fun `returns offender details for a crn`() {
        val person = PersonGenerator.DEFAULT_FULL
        OffenderAliasGenerator.DEFAULT
        val disability = PersonGenerator.DISABILITY
        PersonGenerator.PROVISION

        val detail = mockMvc.get("/offenders/crn/${person.crn}/all")
        { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<OffenderDetail>()

        assertThat(detail.preferredName, equalTo("Frank"))
        assertThat(detail.currentTier, equalTo("B2"))
        assertThat(detail.dateOfBirth, equalTo(LocalDate.of(1980, 1, 1)))
        assertThat(detail.firstName, equalTo("Jack"))
        assertThat(detail.offenderProfile.gender, equalTo("Male"))
        assertThat(detail.previousSurname, equalTo("Jones"))
        assertThat(detail.middleNames!![0], equalTo("James"))
        assertThat(detail.middleNames!![1], equalTo("Robert"))
        assertThat(detail.surname, equalTo("Smith"))
        assertThat(detail.title, equalTo("Dr"))

        val alias = detail.offenderAliases!![0]
        assertThat(alias.firstName, equalTo("Jane"))
        assertThat(alias.middleNames, equalTo(listOf("Louise", "Mary")))
        assertThat(alias.surname, equalTo("Smith"))
        assertThat(alias.gender, equalTo("Female"))
        assertThat(alias.dateOfBirth, equalTo(LocalDate.of(1970, 2, 2)))

        val offenderProfile = detail.offenderProfile
        assertThat(offenderProfile.ethnicity, equalTo("White: British/English/Welsh/Scottish/Northern Irish"))
        assertThat(offenderProfile.nationality, equalTo("British"))
        assertThat(offenderProfile.immigrationStatus, equalTo("Refugee"))
        assertThat(offenderProfile.selfDescribedGender, equalTo("Male"))
        assertThat(offenderProfile.religion, equalTo("Other"))
        assertThat(offenderProfile.sexualOrientation, equalTo("Other"))
        assertThat(offenderProfile.secondaryNationality, equalTo("British"))

        val offenderProfileProvisions = offenderProfile.provisions!![0]
        assertThat(offenderProfileProvisions.provisionType.code, equalTo("PROVTYPE1"))
        assertThat(offenderProfileProvisions.provisionType.description, equalTo("Provision type 1"))
        assertThat(offenderProfileProvisions.startDate, equalTo(LocalDate.of(2026, 1, 1)))
        assertThat(offenderProfileProvisions.lastUpdatedDate, equalTo(LocalDate.of(2026, 2, 2)))
        assertThat(offenderProfileProvisions.finishDate, equalTo(LocalDate.of(2027, 3, 3)))
        assertThat(offenderProfileProvisions.category!!.code, equalTo("PROVCAT1"))
        assertThat(
            offenderProfileProvisions.category!!.description,
            equalTo("Provision category 1")
        )
        assertThat(offenderProfileProvisions.notes, equalTo(null))

        val offenderProfileDisabilities = offenderProfile.disabilities!![0]
        assertThat(offenderProfileDisabilities.disabilityType.code, equalTo("DIS1"))
        assertThat(offenderProfileDisabilities.disabilityType.description, equalTo("Disability type 1"))
        assertThat(offenderProfileDisabilities.startDate, equalTo(LocalDate.of(2024, 12, 13)))
        assertThat(
            offenderProfileDisabilities.lastUpdatedDateTime,
            equalTo(LocalDateTime.of(2023, 4, 21, 12, 54, 3, 0))
        )
        assertThat(offenderProfileDisabilities.disabilityCondition!!.code, equalTo("DISCON1"))
        assertThat(
            offenderProfileDisabilities.disabilityCondition!!.description,
            equalTo("Disability Condition 1")
        )
        assertThat(offenderProfileDisabilities.notes, equalTo(null))
        assertThat(offenderProfileDisabilities.endDate, equalTo(LocalDate.of(2019, 12, 25)))
        assertThat(offenderProfileDisabilities.isActive, equalTo(false))

        val offenderLanguages = detail.offenderProfile.offenderLanguages
        assertThat(offenderLanguages.requiresInterpreter, equalTo(true))
        assertThat(offenderLanguages.primaryLanguage, equalTo("English"))
        assertThat(offenderLanguages.languageConcerns, equalTo("A concern"))

        val otherIds = detail.otherIds
        assertThat(otherIds.immigrationNumber, equalTo("1234567890"))
        assertThat(otherIds.niNumber, equalTo("ABCD-1234"))
        assertThat(otherIds.mostRecentPrisonerNumber, equalTo("A1234BC"))
        assertThat(otherIds.nomsNumber, equalTo("NOMS123"))
        assertThat(otherIds.croNumber, equalTo("CRO1234"))
        assertThat(otherIds.pncNumber!!.trim(), equalTo("PNC12345"))

        val contactDetails = detail.contactDetails
        assertThat(contactDetails.allowSMS, equalTo(true))
        assertThat(contactDetails.emailAddresses!![0], equalTo("test@test.none"))
        assertThat(contactDetails.phoneNumbers!![0].number, equalTo("01234567890"))
        assertThat(contactDetails.phoneNumbers!![0].type, equalTo("TELEPHONE"))
        assertThat(contactDetails.phoneNumbers!![1].number, equalTo("09876543210"))
        assertThat(contactDetails.phoneNumbers!![1].type, equalTo("MOBILE"))
        assertThat(contactDetails.addresses!![0].addressNumber, equalTo("16"))
        assertThat(contactDetails.addresses!![0].streetName, equalTo("Hope Street"))
        assertThat(contactDetails.addresses!![0].postcode, equalTo("MB03 3PS"))
        assertThat(contactDetails.addresses!![0].telephoneNumber, equalTo("01234567716"))

        assertThat(detail.currentExclusion, equalTo(true))
        assertThat(detail.currentRestriction, equalTo(true))
        assertThat(detail.exclusionMessage, equalTo("Exclusion message"))
        assertThat(detail.restrictionMessage, equalTo("Restriction message"))
    }
}