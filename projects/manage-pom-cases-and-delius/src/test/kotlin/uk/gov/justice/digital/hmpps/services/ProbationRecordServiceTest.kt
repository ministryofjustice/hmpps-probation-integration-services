package uk.gov.justice.digital.hmpps.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.CaseAllocationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.set

@ExtendWith(MockitoExtension::class)
internal class ProbationRecordServiceTest {
    @Mock
    private lateinit var personRepository: PersonRepository

    @Mock
    private lateinit var ldapTemplate: LdapTemplate

    @Mock
    private lateinit var caseAllocationRepository: CaseAllocationRepository

    @Mock
    private lateinit var registrationRepository: RegistrationRepository

    @InjectMocks
    private lateinit var service: ProbationRecordService

    @Test
    fun `no user results in null email`() {
        val person = PersonGenerator.generate("N123456", "N1234SR")
        PersonManagerGenerator.generate(
            staff = ProviderGenerator.generateStaff("NoUser", "No", "User"),
            person = person,
        ).also { person.set("managers", listOf(it)) }

        whenever(personRepository.findByNomsId(person.nomsId!!)).thenReturn(person)

        service.findByIdentifier(Identifier(person.nomsId!!))

        verify(ldapTemplate, never()).search(any(), any<AttributesMapper<String?>>())
    }
}
