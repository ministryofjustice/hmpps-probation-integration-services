package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.PersonalContact

interface PersonalContactRepository : JpaRepository<PersonalContact, Long> {
    @EntityGraph(attributePaths = ["type"])
    fun findByPersonIdAndTypeCode(personId: Long, code: String): List<PersonalContact>

    fun findEmergencyContacts(personId: Long) =
        findByPersonIdAndTypeCode(personId, PersonalContact.EMERGENCY_CONTACT)
}
