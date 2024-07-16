package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.security.access.AccessDeniedException
import uk.gov.justice.digital.hmpps.api.model.LaoAccess
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository

@ExtendWith(MockitoExtension::class)
class LaoServiceTest {

    @Mock
    internal lateinit var personRepository: PersonRepository

    @Test
    fun `person restricted throws exception`() {
        whenever(personRepository.findByCrn(PersonGenerator.RESTRICTED_CASE.crn)).thenReturn(PersonGenerator.RESTRICTED_CASE)
        val laoService = LaoService(LaoAccess(false, false), personRepository)
        assertThrows<AccessDeniedException> { laoService.checkLao(PersonGenerator.RESTRICTED_CASE.crn) }
    }

    @Test
    fun `person restricted does not throw exception`() {
        whenever(personRepository.findByCrn(PersonGenerator.RESTRICTED_CASE.crn)).thenReturn(PersonGenerator.RESTRICTED_CASE)
        val laoService = LaoService(LaoAccess(false, true), personRepository)
        assertDoesNotThrow { laoService.checkLao(PersonGenerator.RESTRICTED_CASE.crn) }
    }

    @Test
    fun `person excluded throws exception`() {
        whenever(personRepository.findByCrn(PersonGenerator.EXCLUDED_CASE.crn)).thenReturn(PersonGenerator.EXCLUDED_CASE)
        val laoService = LaoService(LaoAccess(false, false), personRepository)
        assertThrows<AccessDeniedException> { laoService.checkLao(PersonGenerator.EXCLUDED_CASE.crn) }
    }

    @Test
    fun `person excluded does not throw exception`() {
        whenever(personRepository.findByCrn(PersonGenerator.EXCLUDED_CASE.crn)).thenReturn(PersonGenerator.EXCLUDED_CASE)
        val laoService = LaoService(LaoAccess(true, false), personRepository)
        assertDoesNotThrow { laoService.checkLao(PersonGenerator.EXCLUDED_CASE.crn) }
    }
}
