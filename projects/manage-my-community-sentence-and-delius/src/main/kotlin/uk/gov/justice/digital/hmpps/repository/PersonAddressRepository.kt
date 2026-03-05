package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.address.PersonAddress

interface PersonAddressRepository : JpaRepository<PersonAddress, Long> {
    @EntityGraph(attributePaths = ["status"])
    fun findFirstByPersonIdAndStatusCode(personId: Long, code: String): PersonAddress?

    fun findMainAddress(personId: Long) =
        findFirstByPersonIdAndStatusCode(personId, PersonAddress.MAIN_ADDRESS_STATUS)
}
