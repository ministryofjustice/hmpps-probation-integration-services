package uk.gov.justice.digital.hmpps.integrations.delius.recommendation

import IdGenerator
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.entity.Person

@ExtendWith(MockitoExtension::class)
internal class RecommendationStartedTest {

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @InjectMocks
    lateinit var recommendationStarted: RecommendationStarted

    @Test
    fun `exception thrown if no person manager`() {
        val crn = "T123456"
        val person = Person(IdGenerator.getAndIncrement(), crn)
        whenever(personRepository.findByCrn(crn)).thenReturn(person)

        val ex = assertThrows<IllegalStateException> { recommendationStarted.recommended(crn, "anyUrl") }
        assertThat(ex.message, equalTo("No Active Person Manager"))
    }
}
