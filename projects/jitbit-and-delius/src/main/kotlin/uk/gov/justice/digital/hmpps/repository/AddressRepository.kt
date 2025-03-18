package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.Address

interface AddressRepository : JpaRepository<Address, Long> {
    @EntityGraph(attributePaths = ["status"])
    fun findAddressByPersonIdAndStatusCode(personId: Long, statusCode: String): Address?
}

fun AddressRepository.findMainAddress(personId: Long) = findAddressByPersonIdAndStatusCode(personId, "M")
