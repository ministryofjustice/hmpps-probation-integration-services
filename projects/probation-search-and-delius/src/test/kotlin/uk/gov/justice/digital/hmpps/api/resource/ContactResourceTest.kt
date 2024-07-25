package uk.gov.justice.digital.hmpps.api.resource

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.ContactJsonResponse
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.entity.ContactJson
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import java.io.StringReader
import java.sql.Clob

@ExtendWith(MockitoExtension::class)
internal class ContactResourceTest {
    @Mock
    lateinit var personRepository: PersonRepository

    @InjectMocks
    lateinit var contactResource: ContactResource

    @Test
    fun `throws 404 when person not found`() {
        whenever(personRepository.findByCrn(any())).thenReturn(null)
        val exception = assertThrows<NotFoundException> { contactResource.getContacts("A123456") }
        assertThat(exception.message, equalTo("Person with CRN of A123456 not found"))
    }

    @Test
    fun `returns contacts`() {
        whenever(personRepository.findByCrn(any())).thenReturn(PersonGenerator.DEFAULT)
        whenever(personRepository.getContacts(PersonGenerator.DEFAULT.id)).thenReturn(listOf(object : ContactJson {
            override val contactId: Long get() = 1
            override val json: Clob
                get() = mock(Clob::class.java).apply {
                    whenever(characterStream).thenReturn(StringReader("{}"))
                }
        }))

        val results = contactResource.getContacts("A123456")
        assertThat(results, hasSize(1))
        assertThat(results[0], equalTo(ContactJsonResponse(1, "{}")))
    }
}
