package uk.gov.justice.digital.hmpps.integrations.delius.release

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManagerRepository
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class PersonDiedTest {

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var personManagerRepository: PersonManagerRepository

    @Mock
    lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var contactAlertRepository: ContactAlertRepository

    @InjectMocks
    lateinit var personDied: PersonDied

    @ParameterizedTest
    @MethodSource("peopleAndExceptions")
    fun `correct exceptions thrown based on noms number search`(results: List<Person>, e: IgnorableMessageException) {
        val noms = "T2354EX"
        whenever(personRepository.findByNomsNumberAndSoftDeletedIsFalse(noms))
            .thenReturn(results)

        val ex = assertThrows<IgnorableMessageException> { personDied.inCustody(noms, ZonedDateTime.now()) }
        assertThat(ex.message, equalTo(e.message))
    }

    companion object {
        @JvmStatic
        fun peopleAndExceptions() = listOf(
            Arguments.of(listOf<Person>(), IgnorableMessageException("MissingNomsNumber")),
            Arguments.of(
                listOf(PersonGenerator.RECALLABLE, PersonGenerator.RELEASABLE),
                IgnorableMessageException("DuplicateNomsNumber")
            )
        )
    }
}
