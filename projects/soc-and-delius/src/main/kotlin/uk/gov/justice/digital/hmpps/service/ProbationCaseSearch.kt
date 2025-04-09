package uk.gov.justice.digital.hmpps.service

import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.model.Team

@Service
class ProbationCaseSearch(val personRepository: DetailRepository) {
    fun find(request: SearchRequest): ProbationCases =
        ProbationCases(personRepository.findAll(request.asSpecification()).map { it.toProbationCase() })

    fun crns(crns: Set<String>): ProbationCases =
        ProbationCases(personRepository.findByCrnIn(crns).map { it.toProbationCase() })
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

private fun DetailPerson.toProbationCase() = ProbationCase(
    forename,
    surname,
    dateOfBirth,
    crn,
    nomsNumber,
    pncNumber,
    personManager.first().asManager()
)

private fun PersonManager.asManager() = Manager(
    name = Name(staff.forename, staff.middleName, staff.surname),
    team = Team(
        code = team.code,
        localDeliveryUnit = Ldu(
            code = team.district.code,
            name = team.district.description
        )
    ),
    provider = Provider(probationArea.code, probationArea.description)
)