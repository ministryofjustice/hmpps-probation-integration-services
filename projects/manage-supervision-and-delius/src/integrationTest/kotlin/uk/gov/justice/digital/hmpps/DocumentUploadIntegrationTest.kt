package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.APPT_CT_3
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.generateContact
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactRepository
import java.time.LocalDateTime
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class DocumentUploadIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var contactRepository: ContactRepository

    @Test
    fun `update contact with document`() {
        val person = OVERVIEW

        val contact = generateContact(
            person,
            APPT_CT_3,
            ZonedDateTime.of(LocalDateTime.now(EuropeLondon), EuropeLondon),
            description = "add document contact"
        )

        contactRepository.save(contact)
        contactRepository.delete(contact)

    }
}