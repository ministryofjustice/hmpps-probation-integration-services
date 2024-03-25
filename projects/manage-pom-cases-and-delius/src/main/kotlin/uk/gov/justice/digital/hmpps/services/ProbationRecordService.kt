package uk.gov.justice.digital.hmpps.services

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.ProbationRecord
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.CaseAllocationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getByNomsId
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.findMappaRegistration
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername
import uk.gov.justice.digital.hmpps.services.mapping.record

@Service
class ProbationRecordService(
    private val personRepository: PersonRepository,
    private val caseAllocationRepository: CaseAllocationRepository,
    private val registrationRepository: RegistrationRepository,
    private val ldapTemplate: LdapTemplate
) {
    fun findByIdentifier(identifier: Identifier): ProbationRecord {
        val person = when (identifier.type) {
            Identifier.Type.CRN -> personRepository.getByCrn(identifier.value)
            Identifier.Type.NOMS -> personRepository.getByNomsId(identifier.value)
        }
        val user = person.manager.staff.user
        user?.username?.also {
            user.email = ldapTemplate.findEmailByUsername(it)
        }
        val decision = caseAllocationRepository.findLatestActiveDecision(person.id)
        val registration = registrationRepository.findMappaRegistration(person.id)
        val vloAssigned = registrationRepository.hasVloAssigned(person.id)
        return person.record(decision, registration, vloAssigned)
    }
}

data class Identifier(val value: String) {
    val type = Type.of(value)

    enum class Type(val regex: Regex) {
        CRN("[A-Z]\\d{6}".toRegex()), NOMS("[A-Z]\\d{4}[A-Z]{2}".toRegex());

        companion object {
            fun of(value: String): Type = entries.firstOrNull { it.regex.matches(value) }
                ?: throw IllegalArgumentException("UnrecognisedIdentifierType")
        }
    }
}
