package uk.gov.justice.digital.controller

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.controller.PersonController
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.service.toPersonResponse

@ExtendWith(MockitoExtension::class)
class PersonControllerTest {
    @Mock
    lateinit var personService: PersonService

    @InjectMocks
    lateinit var controller: PersonController

    @Test
    fun `noms number results in offender found`() {
        whenever(personService.findPerson("A4321BA")).thenReturn(
            PersonGenerator.PERSON_2.toPersonResponse(
                null,
                "Community"
            )
        )
        val person = controller.findPerson("A4321BA")
        assertThat(person, equalTo(PersonGenerator.PERSON_2.toPersonResponse(null, "Community")))
    }
}