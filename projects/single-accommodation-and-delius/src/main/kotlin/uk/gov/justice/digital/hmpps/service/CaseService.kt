package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.model.*

@Service
@Transactional(readOnly = true)
class CaseService(
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val registrationRepository: RegistrationRepository,
    private val keyDateRepository: KeyDateRepository,
    private val userAccessService: UserAccessService
) {
    fun getCase(username: String, crn: String): Case {
        val person = personRepository.findByCrn(crn).orNotFoundBy("person", crn)

        val manager = personManagerRepository.findFirstByPersonIdAndActiveTrueAndSoftDeletedFalse(person.id)
            .orNotFoundBy("personId", person.id)

        val roshLevel = registrationRepository
            .findByPersonIdInAndTypeCodeIn(listOf(person.id), RegisterType.ROSH_CODES)
            .firstOrNull()
            ?.let { CodeDescription(it.type.code, it.type.description) }

        val access = userAccessService.caseAccessFor(username, crn)

        return Case(
            crn = person.crn,
            name = Name(
                forename = person.firstName,
                middleName = listOfNotNull(person.secondName, person.thirdName).joinToString(" ").ifEmpty { null },
                surname = person.surname
            ),
            dateOfBirth = person.dateOfBirth,
            nomsNumber = person.noms,
            pncNumber = person.pnc,
            staff = Officer(
                name = Name(
                    forename = manager.staff.forename,
                    middleName = manager.staff.middleName,
                    surname = manager.staff.surname
                ),
                username = manager.staff.user?.username ?: "",
                code = manager.staff.code
            ),
            team = CodeDescription(
                code = manager.team.code,
                description = manager.team.description
            ),
            gender = person.gender.description,
            roshLevel = roshLevel,
            expectedReleaseDate = keyDateRepository.findExpectedReleaseDates(person.id),
            userExcluded = access.userExcluded,
            userRestricted = access.userRestricted,
            exclusionMessage = access.exclusionMessage,
            restrictionMessage = access.restrictionMessage
        )
    }
}