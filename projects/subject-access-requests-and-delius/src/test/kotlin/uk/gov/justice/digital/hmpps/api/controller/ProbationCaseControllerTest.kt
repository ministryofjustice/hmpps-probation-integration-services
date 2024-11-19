package uk.gov.justice.digital.hmpps.api.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.Person
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.entity.Person as PersonEntity

@ExtendWith(MockitoExtension::class)
class ProbationCaseControllerTest {

    @Mock
    lateinit var personRepository: PersonRepository

    @InjectMocks
    lateinit var probationCaseController: ProbationCaseController

    @ParameterizedTest
    @CsvSource(
        "A123456,Jon Harry Fred Smith, 1",
        "A123456,Jon Harry Smith, 2",
        "A123456,Jon Fred Smith, 3",
        "A123456,Jon Smith, 4",
    )
    fun `test returned name`(crn: String, fullName: String, person: Int) {

        whenever(personRepository.findByCrn(crn)).thenReturn(getPerson(person))

        val response = probationCaseController.getPersonalDetails(crn)

        val expectedResponse = Person(getName(fullName))

        assertEquals(expectedResponse, response)
    }

    private fun getPerson(person: Int): PersonEntity {
        return when (person) {
            1 -> PersonGenerator.PERSON1
            2 -> PersonGenerator.PERSON2
            3 -> PersonGenerator.PERSON3
            else -> PersonGenerator.PERSON4
        }
    }

    private fun getName(fullName: String): Name {
        val name = fullName.split(" ")
        return when (name.size) {
            4 -> Name(name[0], name[1] + " " + name[2], name[3])
            3 -> Name(name[0], name[1], name[2])
            else -> Name(name[0], surname = name[1])
        }
    }
}