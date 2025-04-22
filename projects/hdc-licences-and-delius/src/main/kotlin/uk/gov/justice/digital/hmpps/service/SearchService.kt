package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.PrisonManager
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.repository.PersonRepository

@Service
class SearchService (private val personRepository: PersonRepository) {

    fun findByListOfNoms(nomsList: List<String>): List<OffenderDetail> {
        return personRepository.findByNomsNumberInAndSoftDeletedIsFalse(nomsList).map { it.toOffenderDetail() }
    }

    fun Person.toOffenderDetail() = OffenderDetail(
        IDs(crn, nomsNumber),
        prisonManager.map { it.toOffenderManager() }

    )

    fun PrisonManager.toOffenderManager() = OffenderManager(
        StaffHuman(staff.code,
            listOfNotNull(
                staff.forename,
                staff.forename2).joinToString(" "),
            staff.surname,
            staff.code.endsWith("U")
        ),
        ProbationArea(probationArea.description),
        active = active
    )
}

