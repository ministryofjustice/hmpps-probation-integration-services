package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.CommunityManagerEntity
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.repository.PersonRepository

@Service
class SearchService(private val personRepository: PersonRepository) {

    fun findByListOfNoms(nomsList: List<String>): List<OffenderDetail> {
        return personRepository.findByNomsNumberInAndSoftDeletedIsFalse(nomsList).map { it.toOffenderDetail() }
    }

    fun Person.toOffenderDetail() = OffenderDetail(
        IDs(crn, nomsNumber),
        communityManagers.map { it.toOffenderManager() }
    )

    fun CommunityManagerEntity.toOffenderManager() = OffenderManager(
        StaffHuman(
            staff.code,
            listOfNotNull(
                staff.forename,
                staff.forename2
            ).joinToString(" "),
            staff.surname,
            staff.code.endsWith("U")
        ),
        ProbationArea(probationArea.description),
        active = active
    )
}

