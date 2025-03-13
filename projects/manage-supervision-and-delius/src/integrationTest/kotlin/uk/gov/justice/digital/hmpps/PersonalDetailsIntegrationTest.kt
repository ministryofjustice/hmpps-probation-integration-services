package uk.gov.justice.digital.hmpps

import io.jsonwebtoken.Jwts
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.ResourceUtils
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.personalDetails.*
import uk.gov.justice.digital.hmpps.api.model.sentence.NoteDetail
import uk.gov.justice.digital.hmpps.aspect.DeliusUserAspect
import uk.gov.justice.digital.hmpps.audit.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.audit.repository.getByCode
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
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PERSON_ADDRESS_1
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PREVIOUS_ADDRESS
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PREVIOUS_ADDRESS_4
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PROVISION_1
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PROVISION_2
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.service.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.time.Duration
import java.time.LocalDate
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class PersonalDetailsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    private var jdbcTemplate: JdbcTemplate = Mockito.mock(JdbcTemplate::class.java)
    private var namedParameterJdbcTemplate: NamedParameterJdbcTemplate = Mockito.mock(NamedParameterJdbcTemplate::class.java)

    @Autowired
    lateinit var deliusUserAspect: DeliusUserAspect

    @Value("\${messaging.producer.topic}")
    lateinit var topicName: String

    @Autowired
    lateinit var auditedInteractionRepository: AuditedInteractionRepository

    @Autowired
    lateinit var businessInteractionRepository: BusinessInteractionRepository

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    lateinit var deliusToken: String

    @BeforeEach
    fun setUp() {
        val keyPair: KeyPair = KeyPairGenerator.getInstance("RSA").apply { this.initialize(2048) }.generateKeyPair()
        deliusToken = Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject("probation-integration-dev")
            .claim("user_name", "DeliusUser")
            .claim("sub", "probation-integration-dev")
            .claim("authorities", listOf("ROLE_PROBATION_INTEGRATION_ADMIN"))
            .expiration(Date(System.currentTimeMillis() + Duration.ofHours(1L).toMillis()))
            .signWith(keyPair.private, Jwts.SIG.RS256)
            .compact()

        deliusUserAspect.set("jdbcTemplate", jdbcTemplate)
        deliusUserAspect.set("namedParameterJdbcTemplate", namedParameterJdbcTemplate)
        Mockito.reset(jdbcTemplate);
        Mockito.reset(namedParameterJdbcTemplate)
    }

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
        assertThat(res.mainAddress?.addressNotes, equalTo(formatNote(PERSON_ADDRESS_1.notes, truncateNote = true)))
        assertThat(res.mainAddress?.verified, equalTo(true))
        assertThat(res.mainAddress?.type, equalTo("Address type 1"))
        assertThat(res.mainAddress?.postcode, equalTo("NE2 56A"))
        assertThat(res.otherAddressCount, equalTo(1))
        assertThat(res.previousAddressCount, equalTo(5))
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
    fun `get main address single note`() {
        val person = PERSONAL_DETAILS
        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/main-address/note/1").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonalDetailsSummary>()

        assertThat(res.crn, equalTo(person.crn))
        assertThat(res.name, equalTo(person.name()))
        assertThat(res.contacts.size, equalTo(1))
        assertThat(res.mainAddress!!.addressNote, equalTo(formatNote(PERSON_ADDRESS_1.notes, truncateNote = false)[1]))
        assertThat(res.otherAddressCount, equalTo(1))
        assertThat(res.previousAddressCount, equalTo(5))
        assertThat(res.telephoneNumber, equalTo("0987657432"))
        assertThat(res.mobileNumber, equalTo("07986789351"))
        assertThat(res.email, equalTo("testemail"))
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
        assertThat(res.previousAddresses[0].to, equalTo(PREVIOUS_ADDRESS_4.endDate))
        assertThat(res.otherAddresses[0].status, equalTo("Another Address"))
    }

    @Test
    fun `previous address with single note is returned`() {
        val person = PERSONAL_DETAILS
        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/addresses/${PREVIOUS_ADDRESS.id}/note/1").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<AddressOverviewSummary>()
        assertThat(res.personSummary, equalTo(person.toSummary()))
        assertThat(res.address!!.postcode, equalTo("NE4 END"))
        assertThat(res.address!!.to, equalTo(PREVIOUS_ADDRESS.endDate))
        assertThat(res.address!!.addressNotes, equalTo(null))
        assertThat(res.address!!.addressNote!!.note, equalTo("previous address note 1"))
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
    fun `circumstance with single note is returned`() {
        val person = PERSONAL_DETAILS
        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/circumstances/${PERSONAL_CIRC_1.id}/note/0").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CircumstanceOverviewSummary>()
        assertThat(res.personSummary, equalTo(person.toSummary()))
        assertThat(res.circumstance, equalTo(PERSONAL_CIRC_1.toCircumstance(true, 0)))
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
            disabilityNote = NoteDetail(1, "Harry Kane", LocalDate.of(2024, 10, 29), "Note 1"),
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
    fun `provision with single note is returned`() {
        val person = PERSONAL_DETAILS
        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/provisions/${PROVISION_1.id}/note/0").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<ProvisionOverviewSummary>()
        assertThat(res.personSummary, equalTo(person.toSummary()))
        assertThat(res.provision, equalTo(PROVISION_1.toProvision(singleNote = true, 0)))
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

        val person = PERSONAL_DETAILS
        mockMvc
            .perform(
                post("/personal-details/${person.crn}/address").withToken()
                    .withJson(
                        PersonAddressEditRequest(
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
        assertThat(res.previousAddresses.size, equalTo(6))
    }

    @ParameterizedTest
    @MethodSource("personContactDetails")
    fun `update contact details for a person`(editRequest: PersonContactEditRequest) {
        val person = PERSONAL_DETAILS

        val expectedResponse = (mockMvc
            .perform(get("/personal-details/${person.crn}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonalDetails>())

        val response = mockMvc
            .perform(
                post("/personal-details/${person.crn}/contact").withToken()
                    .withJson(editRequest)
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonalDetails>()

        assertThat(response.telephoneNumber, equalTo(editRequest.phoneNumber))
        assertThat(response.mobileNumber, equalTo(editRequest.mobileNumber))
        assertThat(response.email, equalTo(editRequest.emailAddress))
        assertThat(response)
            .usingRecursiveComparison().ignoringFields("telephoneNumber", "mobileNumber", "email")
            .isEqualTo(expectedResponse)
    }

    companion object {
        @JvmStatic
        fun personContactDetails() = listOf(
            PersonContactEditRequest(),
            PersonContactEditRequest(
                phoneNumber = "0".repeat(35),
                mobileNumber = "0".repeat(35),
                emailAddress = "X".repeat(255)
            )
        )
    }

    @Test
    @Transactional
    fun `when first main address with no notes - address is created (with delius usertoken)`() {

        val res = mockMvc
            .perform(
                post("/personal-details/X000004/address").withDeliusUserToken(deliusToken)
                    .withJson(
                        PersonAddressEditRequest(
                            postcode = "NE3 NEW",
                            startDate = LocalDate.now().minusDays(10),
                            notes = "",
                        )
                    )
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonalDetails>()

        val updateSqlCaptor = ArgumentCaptor.forClass(String::class.java)
        val sqlCaptor = ArgumentCaptor.forClass(String::class.java)
        val psCaptor = ArgumentCaptor.forClass(MapSqlParameterSource::class.java)

        verify(namedParameterJdbcTemplate, times(1)).update(updateSqlCaptor.capture(), psCaptor.capture())
        verify(jdbcTemplate, times(1)).execute(sqlCaptor.capture())

        val setCallSql = updateSqlCaptor.value
        val clearCallSql = sqlCaptor.value
        val ps = psCaptor.value
        assertThat(setCallSql, equalTo("call pkg_vpd_ctx.set_client_identifier(:dbName)"))
        assertThat(ps.getValue("dbName"), equalTo("DeliusUser"))
        assertThat(clearCallSql, equalTo("call pkg_vpd_ctx.clear_client_identifier()"))
        assertThat(res.mainAddress?.postcode, equalTo("NE3 NEW"))
    }

    @Test
    @Transactional
    fun `when no main address new main address is created (no delius user token)`() {
        val person = PERSONAL_DETAILS
        mockMvc
            .perform(
                post("/personal-details/${person.crn}/address").withToken()
                    .withJson(
                        PersonAddressEditRequest(
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
                post("/personal-details/${person.crn}/address").withToken()
                    .withJson(
                        PersonAddressEditRequest(
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
        assertThat(res.previousAddresses.size, equalTo(6))

        val domainEvents = channelManager.getChannel(topicName).pollFor(2)
        val createAddressEvent =
            domainEvents.firstOrNull { it.eventType == "probation-case.address.created" }?.message as HmppsDomainEvent?

        assertThat(createAddressEvent?.eventType, equalTo("probation-case.address.created"))

        val insertAddressId = businessInteractionRepository.getByCode(BusinessInteractionCode.INSERT_ADDRESS.code)
        val updateAddressId = businessInteractionRepository.getByCode(BusinessInteractionCode.UPDATE_ADDRESS.code)
        val insertAddressAuditRecords =
            auditedInteractionRepository.findAll().filter { it.businessInteractionId == insertAddressId.id }
        val updateAddressAuditRecords =
            auditedInteractionRepository.findAll().filter { it.businessInteractionId == updateAddressId.id }

        assertThat(insertAddressAuditRecords.size, equalTo(1))
        assertThat(updateAddressAuditRecords.size, equalTo(1))
    }

    @Test
    @Transactional
    fun `when all fields are posted for an existing main address all are updated`() {
        val request = PersonAddressEditRequest(
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
        val person = PERSONAL_DETAILS
        val updateResponse = mockMvc
            .perform(
                post("/personal-details/${person.crn}/address").withDeliusUserToken(deliusToken)
                    .withJson(request)
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonalDetails>()

        val domainEvents = channelManager.getChannel(topicName).pollFor(1)
        val updateAddressEvent =
            domainEvents.firstOrNull { it.eventType == "probation-case.address.updated" }?.message as HmppsDomainEvent?

        assertThat(updateAddressEvent?.eventType, equalTo("probation-case.address.updated"))

        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/addresses").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<AddressOverview>()
        assertThat(res.personSummary, equalTo(person.toSummary()))

        val insertAddressId = businessInteractionRepository.getByCode(BusinessInteractionCode.INSERT_ADDRESS.code)
        val insertAddressAuditRecords =
            auditedInteractionRepository.findAll().filter { it.businessInteractionId == insertAddressId.id }
        assertThat(insertAddressAuditRecords.size, equalTo(1))

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

        assertThat(res.previousAddresses.size, equalTo(5))
    }

    @Test
    fun `when address update request does not have a start date`() {
        val request = PersonAddressEditRequest()
        val res = mockMvc.perform(
            post("/personal-details/X000001/address").withToken()
                .withJson(request)
        )
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(res.message, equalTo("Start date must be provided"))
    }

    @Test
    fun `when address update request has a start date later than today`() {
        val request = PersonAddressEditRequest(startDate = LocalDate.now().plusDays(1))
        val res = mockMvc.perform(
            post("/personal-details/X000001/address").withToken()
                .withJson(request)
        )
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(res.message, equalTo("Start date must not be later than today"))
    }

    @Test
    fun `when address update request has an date later than today`() {
        val request = PersonAddressEditRequest(startDate = LocalDate.now(), endDate = LocalDate.now().plusDays(1))
        val res = mockMvc.perform(
            post("/personal-details/X000001/address").withToken()
                .withJson(request)
        )
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(res.message, equalTo("End date must not be later than today"))
    }

    @Test
    fun `when street name greater than 35 chars`() {
        val request = PersonAddressEditRequest(
            startDate = LocalDate.now(),
            streetName = "U".repeat(100)
        )
        val res = mockMvc.perform(
            post("/personal-details/X000001/address").withToken()
                .withJson(request)
        )
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(res.message, equalTo("Validation failure"))
        assertThat(res.fields?.size, equalTo(1))
    }

    @Test
    fun `when person contact greater than max length`() {
        val request = PersonContactEditRequest(
            phoneNumber = "0".repeat(36),
            mobileNumber = "0".repeat(36),
            emailAddress = "X".repeat(256)
        )
        val res = mockMvc.perform(
            post("/personal-details/X000001/contact").withToken()
                .withJson(request)
        )
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(res.message, equalTo("Validation failure"))
        assertThat(res.fields?.size, equalTo(3))
    }
}

fun MockHttpServletRequestBuilder.withDeliusUserToken(token: String) =
    header(HttpHeaders.AUTHORIZATION, "Bearer $token")
