package uk.gov.justice.digital.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.service.toPersonResponse

@ExtendWith(MockitoExtension::class)
class PersonServiceTest {
    @Mock
    lateinit var personRepository: PersonRepository

    @InjectMocks
    lateinit var service: PersonService

    @Test
    fun `invalid crn or noms`() {
        val exception: Exception =
            assertThrows(IllegalArgumentException::class.java) { service.findPerson("sausages") }
        assertThat(exception.message, equalTo("sausages is not a valid crn or prisoner number"))
    }

    @Test
    fun `valid crn results in offender not found`() {
        whenever(personRepository.findByCrn("X123456")).thenReturn(null)
        val exception: Exception =
            assertThrows(NotFoundException::class.java) { service.findPerson("X123456") }
        assertThat(exception.message, equalTo("Person with crn of X123456 not found"))
    }

    @Test
    fun `valid noms number results in offender not found`() {
        whenever(personRepository.findByNomsNumber("A1234AB")).thenReturn(null)
        val exception: Exception =
            assertThrows(NotFoundException::class.java) { service.findPerson("A1234AB") }
        assertThat(exception.message, equalTo("Person with prisoner number of A1234AB not found"))
    }

    @Test
    fun `valid crn results in offender found`() {
        whenever(personRepository.findByCrn("X123456")).thenReturn(PersonGenerator.PERSON_1)
        val person = service.findPerson("X123456")
        assertThat(person, equalTo(PersonGenerator.PERSON_1.toPersonResponse()))
    }

    @Test
    fun `valid noms number results in offender found`() {
        whenever(personRepository.findByNomsNumber("A4321BA")).thenReturn(PersonGenerator.PERSON_2)
        val person = service.findPerson("A4321BA")
        assertThat(person, equalTo(PersonGenerator.PERSON_2.toPersonResponse()))
    }
}
