package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integration.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integration.delius.person.entity.PersonRepository

@ExtendWith(MockitoExtension::class)
class SubjectAccessRequestsServiceTest {

    @Mock
    lateinit var personRepository: PersonRepository

    @InjectMocks
    lateinit var subjectAccessRequestsService: SubjectAccessRequestsService

    val person1 = PersonGenerator.PERSON1

    @ParameterizedTest
    @CsvSource(
        "A123456,Jon Harry Fred Smith, 1",
        "A123456,Jon Harry Smith, 2",
        "A123456,Jon Fred Smith, 3",
        "A123456,Jon Smith, 4",
    )
    fun `test returned name`(crn: String, fullName: String, person: Int) {

        whenever(personRepository.findByCrn(crn)).thenReturn(getPerson(person))

        val response = subjectAccessRequestsService.getPersonDetailsByCrn(crn)

        assertEquals(fullName, response.fullName)

    }

    private fun getPerson(person: Int): Person {
        return when (person) {
            1 -> PersonGenerator.PERSON1
            2 -> PersonGenerator.PERSON2
            3 -> PersonGenerator.PERSON3
            else -> PersonGenerator.PERSON4
        }
    }
}