package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.staff.OfficeLocation
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface OfficeLocationRepository : JpaRepository<OfficeLocation, Long> {
    fun findByCode(code: String): OfficeLocation?

    @Query(
        """
        select o from OfficeLocation o
        join fetch o.localAdminUnit lau
        join fetch lau.probationDeliveryUnit pdu
        where pdu.regionId = :regionId and pdu.selectable = true and lau.selectable = true
    """
    )
    fun findByRegionId(regionId: Long): List<OfficeLocation>

    @Query(
        """
        select o from OfficeLocation o
        join fetch o.localAdminUnit lau
        join fetch lau.probationDeliveryUnit pdu
        where pdu.code = :pduCode and pdu.selectable = true and lau.selectable = true
        """

    )
    fun findByPduCode(pduCode: String): List<OfficeLocation>
}

fun OfficeLocationRepository.getByCode(code: String): OfficeLocation =
    findByCode(code) ?: throw NotFoundException("OfficeLocation", "code", code)