package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.util.ResourceUtils
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.personalDetails.*
import uk.gov.justice.digital.hmpps.api.model.sentence.NoteDetail
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator.AUDIT_USER
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.ALIAS_1
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.DISABILITY_1
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.DISABILITY_2
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PERSONAL_CIRC_1
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PERSONAL_CIRC_2
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PERSONAL_CIRC_PREV
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PERSONAL_CONTACT_1
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PERSONAL_DETAILS
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PREVIOUS_ADDRESS
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PROVISION_1
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PROVISION_2
import uk.gov.justice.digital.hmpps.service.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class PersonalDetailsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var transactionManager: PlatformTransactionManager

    lateinit var transactionTemplate: TransactionTemplate

    @Test
    fun `personal details are returned`() {

        val person = PERSONAL_DETAILS
        val res = mockMvc
            .perform(get("/personal-details/${person.crn}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonalDetails>()
        assertThat(res.crn, equalTo(person.crn))
        assertThat(res.noms, equalTo(person.noms))
        assertThat(res.name, equalTo(Name("Caroline", "Louise", "Bloggs")))
        assertThat(res.preferredName, equalTo("Caz"))
        assertThat(res.preferredGender, equalTo("Female"))
        assertThat(res.religionOrBelief, equalTo("Christian"))
        assertThat(res.preferredLanguage, equalTo("Arabic"))
        assertThat(res.previousSurname, equalTo("Smith"))
        assertThat(res.sexualOrientation, equalTo("Heterosexual"))
        assertThat(res.mainAddress?.status, equalTo("Main Address"))
        assertThat(res.mainAddress?.notes, equalTo("Some Notes"))
        assertThat(res.mainAddress?.verified, equalTo(true))
        assertThat(res.mainAddress?.type, equalTo("Address type 1"))
        assertThat(res.mainAddress?.postcode, equalTo("NE2 56A"))
        assertThat(res.otherAddressCount, equalTo(1))
        assertThat(res.previousAddressCount, equalTo(1))
        assertThat(res.contacts.size, equalTo(1))
        assertThat(res.contacts[0].contactId, equalTo(PERSONAL_CONTACT_1.id))
        assertThat(res.contacts[0].name, equalTo(Name("Sam", "Steven", "Smith")))
        assertThat(res.contacts[0].address?.postcode, equalTo("NE1 56A"))
        assertThat(res.contacts[0].relationship, equalTo("Brother"))
        assertThat(res.contacts[0].relationshipType, equalTo("Family Member"))
        assertThat(res.circumstances.circumstances.size, equalTo(2))
        assertThat(res.circumstances.lastUpdated, equalTo(LocalDate.now().minusDays(1)))
        assertThat(res.circumstances.circumstances[0].type, equalTo("Employed"))
        assertThat(res.circumstances.circumstances[0].subType, equalTo("Full-time employed (30 or more hours per week"))
        assertThat(res.circumstances.circumstances[1].type, equalTo("Owns house"))
        assertThat(res.circumstances.circumstances[1].subType, equalTo("Has children"))
        assertThat(res.disabilities.lastUpdated, equalTo(LocalDate.now().minusDays(1)))
        assertThat(res.disabilities.disabilities[0], equalTo("Some Illness"))
        assertThat(res.disabilities.disabilities[1], equalTo("Blind"))
        assertThat(res.provisions.lastUpdated, equalTo(LocalDate.now().minusDays(1)))
        assertThat(res.provisions.provisions[0], equalTo("Braille"))
        assertThat(res.provisions.provisions[1], equalTo("Lots of breaks"))
        assertThat(res.documents.size, equalTo(2))
        assertThat(res.documents[0].name, equalTo("induction.doc"))
        assertThat(res.documents[1].name, equalTo("other.doc"))
        assertThat(res.documents[0].id, equalTo("A001"))
        assertThat(res.documents[1].id, equalTo("A002"))
        assertThat(res.aliases[0].forename, equalTo(ALIAS_1.forename))
        assertThat(res.genderIdentity, equalTo("Test Gender Identity"))
        assertThat(res.selfDescribedGender, equalTo("Some gender description"))
        assertThat(res.requiresInterpreter, equalTo(true))
    }

    @Test
    fun `not found status returned`() {
        mockMvc
            .perform(get("/personal-details/X123456").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(get("/personal-details/X000005"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `document can be downloaded`() {
        mockMvc.perform(get("/personal-details/X000005/document/A001").accept("application/octet-stream").withToken())
            .andExpect(MockMvcResultMatchers.request().asyncStarted())
            .andDo(MvcResult::getAsyncResult)
            .andExpect(status().is2xxSuccessful)
            .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/msword;charset=UTF-8"))
            .andExpect(
                MockMvcResultMatchers.header().string(
                    "Content-Disposition",
                    "attachment; filename=\"=?UTF-8?Q?induction.doc?=\"; filename*=UTF-8''induction.doc"
                )
            )
            .andExpect(MockMvcResultMatchers.header().doesNotExist("Custom-Alfresco-Header"))
            .andExpect(
                MockMvcResultMatchers.content()
                    .bytes(ResourceUtils.getFile("classpath:simulations/__files/document.pdf").readBytes())
            )
    }

    @Test
    fun `document can not be found`() {
        mockMvc.perform(get("/personal-details/X000005/document/A010").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `personal summary is returned`() {

        val person = PERSONAL_DETAILS
        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/summary").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonSummary>()
        assertThat(res.crn, equalTo(person.crn))
        assertThat(res.pnc, equalTo(person.pnc))
        assertThat(res.dateOfBirth, equalTo(person.dateOfBirth))
        assertThat(res.name, equalTo(Name(person.forename, person.secondName, person.surname)))
    }

    @Test
    fun `personal contact is returned`() {
        val person = PERSONAL_DETAILS
        val contact = PERSONAL_CONTACT_1
        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/personal-contact/${contact.id}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonalContact>()

        assertThat(res, equalTo(contact.toContact()))
    }

    @Test
    fun `personal contact single note is returned`() {
        val person = PERSONAL_DETAILS
        val contact = PERSONAL_CONTACT_1
        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/personal-contact/${contact.id}/note/0").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonalContact>()

        assertThat(res, equalTo(contact.toContact(true, 0)))
    }

    @Test
    fun `personal contact single note not found`() {
        val person = PERSONAL_DETAILS
        val contact = PERSONAL_CONTACT_1
        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/personal-contact/${contact.id}/note/10").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonalContact>()

        assertThat(res, equalTo(contact.toContact(true, 0)))
    }

    @Test
    fun `personal summary not found`() {
        mockMvc
            .perform(get("/personal-details/X999999/summary").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `personal contact not found`() {
        mockMvc
            .perform(get("/personal-details/X999999/personal-contact/999999999").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `addresses are returned`() {
        val person = PERSONAL_DETAILS
        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/addresses").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<AddressOverview>()
        assertThat(res.personSummary, equalTo(person.toSummary()))
        assertThat(res.mainAddress?.postcode, equalTo("NE2 56A"))
        assertThat(res.previousAddresses[0].postcode, equalTo("NE4 END"))
        assertThat(res.previousAddresses[0].to, equalTo(PREVIOUS_ADDRESS.endDate))
        assertThat(res.otherAddresses[0].status, equalTo("Another Address"))
    }

    @Test
    fun `addresses person not found`() {
        mockMvc
            .perform(get("/personal-details/X999999/addresses").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `circumstances are returned`() {
        val person = PERSONAL_DETAILS
        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/circumstances").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CircumstanceOverview>()
        assertThat(res.personSummary, equalTo(person.toSummary()))
        assertThat(res.circumstances[0], equalTo(PERSONAL_CIRC_1.toCircumstance()))
        assertThat(res.circumstances[1], equalTo(PERSONAL_CIRC_2.toCircumstance()))
        assertThat(res.circumstances[2], equalTo(PERSONAL_CIRC_PREV.toCircumstance()))
    }

    @Test
    fun `circumstances not found`() {
        mockMvc
            .perform(get("/personal-details/X999999/circumstances").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `disabilities are returned`() {
        val person = PERSONAL_DETAILS
        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/disabilities").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<DisabilityOverview>()
        assertThat(res.personSummary, equalTo(person.toSummary()))
        assertThat(res.disabilities!![0], equalTo(DISABILITY_1.toDisability(0)))
        assertThat(res.disabilities!![1], equalTo(DISABILITY_2.toDisability(1)))
    }

    @Test
    fun `disability returned single note`() {
        val person = PERSONAL_DETAILS

        val expected = Disability(
            0,
            DISABILITY_1.type.description,
            disabilityNote = NoteDetail(1, "Harry Kane", LocalDate.of(2024,10,29), "Note 1"),
            startDate = DISABILITY_1.startDate,
            lastUpdated = DISABILITY_1.lastUpdated,
            lastUpdatedBy = Name(forename = USER.forename, surname = USER.surname)
        )

        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/disability/0/note/1").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<DisabilityOverview>()
        assertThat(res.personSummary, equalTo(person.toSummary()))
        assertThat(res.disability, equalTo(expected))
    }

    @Test
    fun `disability with no note`() {
        val person = PERSONAL_DETAILS

        val expected = DisabilityOverview(
            person.toSummary(),
            disability = Disability(
                0,
                DISABILITY_1.type.description,
                startDate = DISABILITY_1.startDate,
                lastUpdated = DISABILITY_1.lastUpdated,
                lastUpdatedBy = Name(forename = USER.forename, surname = USER.surname)
            )
        )

        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/disability/0/note/10").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<DisabilityOverview>()
        assertThat(res, equalTo(expected))
    }

    @Test
    fun `person summary only when disability not found`() {
        val person = PERSONAL_DETAILS

        val expected = DisabilityOverview(person.toSummary())

        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/disability/10/note/1").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<DisabilityOverview>()
        assertThat(res, equalTo(expected))
    }

    @Test
    fun `disabilities not found`() {
        mockMvc
            .perform(get("/personal-details/X999999/disabilities").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `provisions are returned`() {
        val person = PERSONAL_DETAILS
        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/provisions").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<ProvisionOverview>()
        assertThat(res.personSummary, equalTo(person.toSummary()))
        assertThat(res.provisions[0], equalTo(PROVISION_1.toProvision()))
        assertThat(res.provisions[1], equalTo(PROVISION_2.toProvision()))
    }

    @Test
    fun `provisions not found`() {
        mockMvc
            .perform(get("/personal-details/X999999/provisions").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    @Transactional
    fun `main address updated with valid end date results in no main address and more previous addresses`() {
        transactionTemplate = TransactionTemplate(transactionManager)
        transactionTemplate.execute {
            val person = PERSONAL_DETAILS
            mockMvc
                .perform(
                    post("/personal-details/${person.crn}").withToken()
                        .withJson(
                            PersonalContactEditRequest(
                                postcode = "NE1 UPD",
                                startDate = LocalDate.now().minusDays(10),
                                endDate = LocalDate.now()

                            )
                        )
                )
                .andExpect(status().isOk)
                .andReturn().response.contentAsJson<PersonalDetails>()
            val res = mockMvc
                .perform(get("/personal-details/${person.crn}/addresses").withToken())
                .andExpect(status().isOk)
                .andReturn().response.contentAsJson<AddressOverview>()
            assertThat(res.personSummary, equalTo(person.toSummary()))
            assertThat(res.mainAddress, equalTo(null))
            assertThat(res.previousAddresses.size, equalTo(2))
        }
    }

    @Test
    @Transactional
    fun `when no main address new main address is created`() {
        transactionTemplate = TransactionTemplate(transactionManager)
        transactionTemplate.execute {
            val person = PERSONAL_DETAILS
            mockMvc
                .perform(
                    post("/personal-details/${person.crn}").withToken()
                        .withJson(
                            PersonalContactEditRequest(
                                postcode = "NE1 UPD",
                                startDate = LocalDate.now().minusDays(10),
                                endDate = LocalDate.now()

                            )
                        )
                )
                .andExpect(status().isOk)
                .andReturn().response.contentAsJson<PersonalDetails>()
            mockMvc
                .perform(
                    post("/personal-details/${person.crn}").withToken()
                        .withJson(
                            PersonalContactEditRequest(
                                postcode = "NE1 NEW",
                                startDate = LocalDate.now().minusDays(9),
                                endDate = null

                            )
                        )
                )
                .andExpect(status().isOk)
                .andReturn().response.contentAsJson<PersonalDetails>()
            val res = mockMvc
                .perform(get("/personal-details/${person.crn}/addresses").withToken())
                .andExpect(status().isOk)
                .andReturn().response.contentAsJson<AddressOverview>()
            assertThat(res.personSummary, equalTo(person.toSummary()))
            assertThat(res.mainAddress?.postcode, equalTo("NE1 NEW"))
            assertThat(res.previousAddresses.size, equalTo(2))
        }
    }

    @Test
    @Transactional
    fun `when all fields are posted for an existing main address all are updated`() {
        val request = PersonalContactEditRequest(
            phoneNumber = "0191255446",
            mobileNumber = "077989988",
            emailAddress = "updated@test.none",
            buildingName = "Building",
            buildingNumber = "23",
            streetName = "The Street",
            district = "A District",
            town = "Town",
            county = "County",
            postcode = "NE1 UPD",
            addressTypeCode = PersonDetailsGenerator.PERSON_ADDRESS_TYPE_1.code,
            verified = false,
            noFixedAddress = false,
            startDate = LocalDate.now().minusDays(10),
            notes = "This has been updated for testing"
        )
        transactionTemplate = TransactionTemplate(transactionManager)
        transactionTemplate.execute {
            val person = PERSONAL_DETAILS
            val updateResponse = mockMvc
                .perform(
                    post("/personal-details/${person.crn}").withToken()
                        .withJson(request)
                )
                .andExpect(status().isOk)
                .andReturn().response.contentAsJson<PersonalDetails>()
            val res = mockMvc
                .perform(get("/personal-details/${person.crn}/addresses").withToken())
                .andExpect(status().isOk)
                .andReturn().response.contentAsJson<AddressOverview>()
            assertThat(res.personSummary, equalTo(person.toSummary()))

            assertThat(updateResponse.telephoneNumber, equalTo(request.phoneNumber))
            assertThat(updateResponse.mobileNumber, equalTo(request.mobileNumber))
            assertThat(updateResponse.email, equalTo(request.emailAddress))
            assertThat(updateResponse.lastUpdated, equalTo(LocalDate.now()))
            assertThat(
                updateResponse.lastUpdatedBy,
                equalTo(Name(forename = AUDIT_USER.forename, surname = AUDIT_USER.surname))
            )

            assertThat(res.mainAddress?.buildingName, equalTo(request.buildingName))
            assertThat(res.mainAddress?.buildingNumber, equalTo(request.buildingNumber))
            assertThat(res.mainAddress?.streetName, equalTo(request.streetName))
            assertThat(res.mainAddress?.district, equalTo(request.district))
            assertThat(res.mainAddress?.town, equalTo(request.town))
            assertThat(res.mainAddress?.county, equalTo(request.county))
            assertThat(res.mainAddress?.postcode, equalTo(request.postcode))
            assertThat(res.mainAddress?.type, equalTo(PersonDetailsGenerator.PERSON_ADDRESS_TYPE_1.description))
            assertThat(res.mainAddress?.verified, equalTo(request.verified))
            assertThat(res.mainAddress?.noFixedAddress, equalTo(request.noFixedAddress))
            assertThat(res.mainAddress?.lastUpdated, equalTo(LocalDate.now()))
            assertThat(
                res.mainAddress?.lastUpdatedBy,
                equalTo(Name(forename = AUDIT_USER.forename, surname = AUDIT_USER.surname))
            )

            assertThat(res.previousAddresses.size, equalTo(1))
        }
    }

    @Test
    fun `when personal details update request does not have a start date`() {
        val request = PersonalContactEditRequest()
        val res = mockMvc.perform(
            post("/personal-details/X000001").withToken()
                .withJson(request)
        )
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(res.message, equalTo("Start date must be provided"))
    }

    @Test
    fun `when personal details update request has a start date later than today`() {
        val request = PersonalContactEditRequest(startDate = LocalDate.now().plusDays(1))
        val res = mockMvc.perform(
            post("/personal-details/X000001").withToken()
                .withJson(request)
        )
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(res.message, equalTo("Start date must not be later than today"))
    }

    @Test
    fun `when personal details update request has an date later than today`() {
        val request = PersonalContactEditRequest(startDate = LocalDate.now(), endDate = LocalDate.now().plusDays(1))
        val res = mockMvc.perform(
            post("/personal-details/X000001").withToken()
                .withJson(request)
        )
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(res.message, equalTo("End date must not be later than today"))
    }

    @Test
    fun `when street name greater than 35 chars`() {
        val request = PersonalContactEditRequest(
            startDate = LocalDate.now(),
            streetName = "U".repeat(100),
            emailAddress = "X".repeat(257)
        )
        val res = mockMvc.perform(
            post("/personal-details/X000001").withToken()
                .withJson(request)
        )
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(res.message, equalTo("Validation failure"))
        assertThat(res.fields?.size, equalTo(2))
    }
}
