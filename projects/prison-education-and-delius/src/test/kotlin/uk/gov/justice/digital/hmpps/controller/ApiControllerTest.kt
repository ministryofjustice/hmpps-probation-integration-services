package uk.gov.justice.digital.hmpps.controller

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.COMMUNITY_MANAGER
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.COMMUNITY_MANAGER_WITH_USER
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.PERSON
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.repository.CommunityManagerRepository
import uk.gov.justice.digital.hmpps.repository.PersonRepository

@ExtendWith(MockitoExtension::class)
class ApiControllerTest {
    @Mock
    private lateinit var personRepository: PersonRepository

    @Mock
    private lateinit var communityManagerRepository: CommunityManagerRepository

    @Mock
    private lateinit var ldapTemplate: LdapTemplate

    @InjectMocks
    private lateinit var apiController: ApiController

    @Test
    fun `missing person is thrown`() {
        assertThrows<NotFoundException> {
            apiController.getCommunityManager(PERSON.prisonerId)
        }.run {
            assertThat(message, equalTo("Person with id of A0000AA not found"))
        }
    }

    @Test
    fun `unlinked user account results in null email`() {
        whenever(personRepository.findByPrisonerId(PERSON.prisonerId)).thenReturn(PERSON)
        whenever(communityManagerRepository.findByPersonId(PERSON.id)).thenReturn(COMMUNITY_MANAGER)

        apiController.getCommunityManager(PERSON.prisonerId).run {
            assertThat(firstName, equalTo("Test"))
            assertThat(lastName, equalTo("Staff"))
            assertThat(email, nullValue())
        }
    }

    @Test
    fun `linked user account returns email`() {
        whenever(personRepository.findByPrisonerId(PERSON.prisonerId)).thenReturn(PERSON)
        whenever(communityManagerRepository.findByPersonId(PERSON.id)).thenReturn(COMMUNITY_MANAGER_WITH_USER)
        whenever(ldapTemplate.search(any(), any<AttributesMapper<String?>>())).thenReturn(listOf("test@example.com"))

        apiController.getCommunityManager(PERSON.prisonerId).run {
            assertThat(firstName, equalTo("Test"))
            assertThat(lastName, equalTo("User"))
            assertThat(email, equalTo("test@example.com"))
        }
    }
}
