package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.CaseAccessList
import uk.gov.justice.digital.hmpps.api.model.User
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.ExclusionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.RestrictionRepository

@Service
class UserService(
    private val personRepository: PersonRepository,
    private val exclusionRepository: ExclusionRepository,
    private val restrictionRepository: RestrictionRepository
) {
    fun getAllAccessLimitations(crn: String, usernamesFilter: List<String>? = null): CaseAccessList {
        val person = personRepository.findByCrnAndSoftDeletedFalse(crn) ?: throw NotFoundException("Person", "crn", crn)
        return CaseAccessList(
            crn = crn,
            exclusionMessage = person.exclusionMessage,
            restrictionMessage = person.restrictionMessage,
            excludedFrom = exclusionRepository.findByPersonId(person.id).map { it.user.username }
                .filter { usernamesFilter == null || it in usernamesFilter }
                .map { User(it) },
            restrictedTo = restrictionRepository.findByPersonId(person.id).map { it.user.username }
                .filter { usernamesFilter == null || it in usernamesFilter }
                .map { User(it) },
        )
    }
}

