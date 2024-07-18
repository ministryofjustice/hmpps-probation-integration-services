package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.conviction.PssRequirements
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity.ConvictionEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity.ConvictionRequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.PssRequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.service.RequirementService

@ExtendWith(MockitoExtension::class)
class RequirementServiceTest {

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var convictionEventRepository: ConvictionEventRepository

    @Mock
    lateinit var convictionRequirementRepository: ConvictionRequirementRepository

    @Mock
    lateinit var pssRequirementRepository: PssRequirementRepository

    @InjectMocks
    lateinit var requirementService: RequirementService

    @Test
    fun `no custody record`() {
        val person = PersonGenerator.CURRENTLY_MANAGED
        whenever(personRepository.findByCrn(person.crn)).thenReturn(person)
        whenever(convictionEventRepository.findEventByIdAndOffenderId(1, person.id)).thenReturn(Event(1, 1))

        val expectedResponse = PssRequirements(listOf())
        val response = requirementService.getPssRequirementsByConvictionId(person.crn, 1)

        assertEquals(expectedResponse, response)

        verifyNoInteractions(pssRequirementRepository)
        verifyNoInteractions(convictionRequirementRepository)
        verifyNoMoreInteractions(personRepository)
        verifyNoMoreInteractions(convictionEventRepository)
    }
}