package uk.gov.justice.digital.hmpps.service

import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.model.*

@Service
class ProbationCaseSearch(val personRepository: DetailRepository) {
    fun find(request: SearchRequest): List<OffenderDetail> =
        personRepository.findAll(request.asSpecification()).map { it.toProbationCase(false) }

    fun crns(crns: Set<String>): List<OffenderDetail> =
        personRepository.findByCrnIn(crns).map { it.toProbationCase(true) }
}

private fun SearchRequest.asSpecification(): Specification<DetailPerson> = listOfNotNull(
    firstName?.let { matchesForename(it) },
    surname?.let { matchesSurname(it) },
    dateOfBirth?.let { matchesDateOfBirth(it) },
    crn?.let { matchesCrn(it) },
    nomsNumber?.let { matchesNomsId(it) },
    pncNumber?.let { matchesPnc(it) }
).ifEmpty { throw IllegalArgumentException("At least one field must be provided") }
    .reduce { a, b -> a.and(b) }

private fun DetailPerson.toProbationCase(includeAliases: Boolean) = OffenderDetail(
    firstName = forename,
    surname = surname,
    dateOfBirth = dateOfBirth,
    gender = gender.description,
    otherIds = IDs(crn, nomsNumber, pncNumber),
    offenderProfile = OffenderProfile(ethnicity?.description, nationality?.description, religion?.description),
    offenderManagers = personManager.map { it.asOffenderManager() },
    offenderAliases = if (includeAliases) offenderAliases.map { it.asProbationAlias() } else emptyList()
)

private fun PersonManager.asOffenderManager() = OffenderManager(
    staff = StaffHuman(staff.code, staff.forename, staff.surname, staff.unallocated),
    team = SearchResponseTeam(team.code, team.description, KeyValue(team.district.code, team.district.description)),
    probationArea = ProbationArea(probationArea.code, probationArea.description, listOf()),
)

private fun PersonAlias.asProbationAlias() = OffenderAlias(
    id = aliasID,
    dateOfBirth = dateOfBirth,
    firstName = firstName,
    middleNames = listOfNotNull(secondName, thirdName),
    surname = surname,
    gender = gender.description
)