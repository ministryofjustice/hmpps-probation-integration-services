package uk.gov.justice.digital.hmpps.service

import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integration.delius.entity.*
import uk.gov.justice.digital.hmpps.model.*

@Service
class ProbationCaseSearch(private val personRepository: PersonRepository) {
    fun find(request: SearchRequest): List<ProbationCaseDetail> = personRepository.findAll(request.asSpecification())
        .map { p ->
            ProbationCaseDetail(
                p.ids(),
                p.forename,
                p.surname,
                p.dateOfBirth,
                p.gender.description,
                listOfNotNull(p.secondName, p.thirdName),
                p.profile(),
                p.contactDetails(),
                p.aliases.map { a -> a.asAlias() },
                p.currentDisposal,
                p.currentRestriction == true,
                p.restrictionMessage,
                p.currentExclusion == true,
                p.exclusionMessage,
            )
        }

    private fun SearchRequest.asSpecification(): Specification<Person> = listOfNotNull(
        if (includeAliases) {
            matchesPerson(firstName, surname, dateOfBirth).or(matchesAlias(firstName, surname, dateOfBirth))
        } else {
            matchesPerson(firstName, surname, dateOfBirth)
        },
        crn?.let { matchesCrnOrPrevious(it) },
        nomsNumber?.let { matchesNomsId(it) },
        pncNumber?.let { matchesPnc(it) }
    ).ifEmpty { throw IllegalArgumentException("At least one field must be provided") }
        .reduce { a, b -> a.and(b) }
}

fun Person.ids() = OtherIds(crn, pnc, nomsId, cro)
fun Person.profile() =
    CaseProfile(
        ethnicity?.description,
        nationality?.description,
        religion?.description,
        sexualOrientation?.description,
        disabilities.map { it.asCaseDisability() }
    )

fun Disability.asCaseDisability() = CaseDisability(
    CodedValue(type.code, type.description),
    condition?.let { CodedValue(it.code, it.description) },
    startDate,
    finishDate,
    notes
)

fun Person.contactDetails() = ContactDetails(
    listOfNotNull(
        telephoneNumber?.let { PhoneNumber(it, "TELEPHONE") },
        mobileNumber?.let { PhoneNumber(it, "MOBILE") },
    ),
    listOfNotNull(emailAddress)
)

fun PersonAlias.asAlias() =
    CaseAlias(firstName, surname, dateOfBirth, gender.description, listOfNotNull(secondName, thirdName))
