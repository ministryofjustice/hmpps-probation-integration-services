package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integration.delius.sentence.entity.CustodyRepository

@ExtendWith(MockitoExtension::class)
internal class SentenceServiceTest {
    @Mock
    internal lateinit var custodyRepository: CustodyRepository

    @InjectMocks
    internal lateinit var sentenceService: SentenceService

    @Test
    fun `no custodial sentence throws exception`() {
        val crn = "T123456"
        whenever(custodyRepository.findAllByDisposalEventPersonCrn(crn))
            .thenReturn(listOf())

        assertThrows<NotFoundException> { sentenceService.findLatestReleaseRecall(crn) }
    }

    @Test
    fun `multiple custodial sentences throws exception`() {
        val person = PersonGenerator.generatePerson("M123456")
        val sentences = listOf(
            SentenceGenerator.generateCustodialSentence(person),
            SentenceGenerator.generateCustodialSentence(person)
        )
        whenever(custodyRepository.findAllByDisposalEventPersonCrn(person.crn)).thenReturn(sentences)

        val ex = assertThrows<ResponseStatusException> { sentenceService.findLatestReleaseRecall(person.crn) }
        assertThat(ex.statusCode, equalTo(HttpStatus.EXPECTATION_FAILED))
    }
}