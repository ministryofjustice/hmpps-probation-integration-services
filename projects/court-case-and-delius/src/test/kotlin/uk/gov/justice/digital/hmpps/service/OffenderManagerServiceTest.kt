package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.ldap.core.LdapTemplate
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PRISON_MANAGER
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.generatePersonManager
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.LdapUser
import uk.gov.justice.digital.hmpps.integrations.delius.service.OffenderManagerService
import javax.naming.ldap.LdapName

@ExtendWith(MockitoExtension::class)
class OffenderManagerServiceTest {

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var ldapTemplate: LdapTemplate

    @Mock
    lateinit var person: Person

    private lateinit var offenderManagerService: OffenderManagerService

    @BeforeEach
    fun setUp() {
        offenderManagerService = OffenderManagerService(personRepository, ldapTemplate)
        val staff = StaffGenerator.generate("N01ABBC", user = StaffGenerator.STAFF_USER)
        whenever(person.offenderManagers).thenReturn(listOf(generatePersonManager(person, staff)))
        whenever(person.prisonOffenderManagers).thenReturn(listOf(PRISON_MANAGER))
        whenever(personRepository.findByCrn(any())).thenReturn(person)
    }

    @Test
    fun `email is null when empty string `() {
        val ldapUser = LdapUser(
            dn = LdapName("cn=test"),
            email = "",
            forename = "",
            surname = "",
            telephoneNumber = "",
            username = "TestUser"
        )
        whenever(ldapTemplate.find(any(), eq(LdapUser::class.java))).thenReturn(listOf(ldapUser))
        val ret = offenderManagerService.getAllOffenderManagersForCrn("CRN", false)
        assertEquals(null, ret[0].staff?.email)
    }

    @Test
    fun `email is null when null `() {
        val ldapUser = LdapUser(
            dn = LdapName("cn=test"),
            email = null,
            forename = "",
            surname = "",
            telephoneNumber = "",
            username = "TestUser"
        )
        whenever(ldapTemplate.find(any(), eq(LdapUser::class.java))).thenReturn(listOf(ldapUser))
        val ret = offenderManagerService.getAllOffenderManagersForCrn("CRN", false)
        assertEquals(null, ret[0].staff?.email)
    }

    @Test
    fun `email is populated `() {
        val ldapUser = LdapUser(
            dn = LdapName("cn=test"),
            email = "test",
            forename = "",
            surname = "",
            telephoneNumber = "",
            username = "TestUser"
        )
        whenever(ldapTemplate.find(any(), eq(LdapUser::class.java))).thenReturn(listOf(ldapUser))
        val ret = offenderManagerService.getAllOffenderManagersForCrn("CRN", false)
        assertEquals("test", ret[0].staff?.email)
    }
}

