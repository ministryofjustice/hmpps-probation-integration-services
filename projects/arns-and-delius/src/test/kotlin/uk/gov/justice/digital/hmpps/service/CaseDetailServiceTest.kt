package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.ArnsResponse
import uk.gov.justice.digital.hmpps.model.OffenceDetail
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class CaseDetailServiceTest {
    @Mock
    lateinit var personRepository: PersonRepository

    @InjectMocks
    lateinit var caseDetailService: CaseDetailService

    @Test
    fun `returns case detail for CRN`() {
        val person = PersonGenerator.generatePersonWithOffences()
        whenever(personRepository.findByCrn(person.crn)).thenReturn(person)

        val result = caseDetailService.getCaseDetail(person.crn)

        assertEquals(
            ArnsResponse(
                crn = "A123456",
                dateOfBirth = LocalDate.of(1984, 5, 12),
                gender = "Male",
                offences = listOf(
                    OffenceDetail(
                        mainOffence = "Murder",
                        sentenceStartDate = LocalDate.of(2026, 3, 1),
                        convictionDate = LocalDate.of(2026, 1, 10),
                    ),
                    OffenceDetail(
                        mainOffence = "Second Offence",
                        sentenceStartDate = LocalDate.of(2026, 4, 15),
                        convictionDate = LocalDate.of(2026, 2, 20),
                    ),
                ),
            ),
            result,
        )
    }

    @Test
    fun `returns no offences when person has no events`() {
        val person = PersonGenerator.generate()
        whenever(personRepository.findByCrn(person.crn)).thenReturn(person)

        val result = caseDetailService.getCaseDetail(person.crn)

        assertEquals(
            ArnsResponse(
                crn = person.crn,
                dateOfBirth = person.dateOfBirth,
                gender = person.gender.description,
                offences = emptyList(),
            ),
            result,
        )
    }


    @Test
    fun `throws not found when person does not exist`() {
        whenever(personRepository.findByCrn("Z123456")).thenReturn(null)

        assertThrows<NotFoundException> {
            caseDetailService.getCaseDetail("Z123456")
        }
    }

}

