package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.ReferralStarted
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.ZonedDateTime
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ReferralStartedIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var nsiRepository: NsiRepository

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Test
    fun `nsi, manager and contacts created when it doesn't already exist`() {
        val person = PersonGenerator.SENTENCED_WITHOUT_NSI
        val referralId = UUID.randomUUID()
        val r1 = ReferralStarted(
            referralId,
            ZonedDateTime.now(),
            "ACC",
            SentenceGenerator.SENTENCE_WITHOUT_NSI.event.id,
            "The referral has been started"
        )

        makeRequest(person, referralId, r1)
        validateNsiAndContacts(person, r1)

        // Duplicate the request to make sure nsi is not recreated
        val r2 = ReferralStarted(
            referralId,
            ZonedDateTime.now(),
            "ACC",
            SentenceGenerator.SENTENCE_WITHOUT_NSI.event.id,
            "The referral has been sent twice"
        )

        makeRequest(person, referralId, r2)
        validateNsiAndContacts(person, r2)
    }

    private fun makeRequest(person: Person, referralId: UUID, request: ReferralStarted) {
        request.set(ReferralStarted::referralId, referralId)
        mockMvc
            .perform(put("/probation-case/${person.crn}/referrals").withToken().withJson(request))
            .andExpect(status().isNoContent)

        validateNsiAndContacts(person, request)
    }

    private fun validateNsiAndContacts(person: Person, request: ReferralStarted) {
        val nsi = nsiRepository.findByPersonCrnAndExternalReference(person.crn, request.urn)
        Assertions.assertNotNull(nsi!!)
        assertThat(nsi.actualStartDate!!, isCloseTo(request.startedAt))
        assertThat(nsi.statusDate, isCloseTo(request.startedAt))
        assertThat(nsi.status.code, equalTo(NsiStatus.Code.IN_PROGRESS.value))
        assertThat(nsi.type.code, equalTo("CRS01"))
        assertThat(nsi.eventId, equalTo(SentenceGenerator.SENTENCE_WITHOUT_NSI.event.id))
        Assertions.assertNotNull(nsi.requirementId)
        assertThat(nsi.notes, equalTo(request.notes))

        val manager = nsi.manager
        assertThat(manager.providerId, equalTo(ProviderGenerator.INTENDED_PROVIDER.id))
        assertThat(manager.teamId, equalTo(ProviderGenerator.INTENDED_TEAM.id))
        assertThat(manager.staffId, equalTo(ProviderGenerator.INTENDED_STAFF.id))
        assertThat(manager.startDate, isCloseTo(nsi.actualStartDate!!))

        val contacts = contactRepository.findAll().filter { it.person.crn == person.crn }
        assertThat(
            contacts.map { it.type.code },
            Matchers.hasItems(
                ContactType.Code.NSI_REFERRAL.value,
                ContactType.Code.NSI_COMMENCED.value,
                ContactType.Code.IN_PROGRESS.value
            )
        )
    }
}
