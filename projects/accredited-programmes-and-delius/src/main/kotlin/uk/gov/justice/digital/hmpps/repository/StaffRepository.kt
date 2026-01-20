package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.service.reportMissing

interface StaffRepository : JpaRepository<Staff, Long> {
    @EntityGraph(attributePaths = ["user"])
    fun findAllByCodeIn(code: Set<String>): List<Staff>

    @Query(
        """
            select 
                s.code as staffCode, 
                s.forename as forename, 
                s.surname as surname,
                t.code as teamCode,
                t.description as teamDescription,
                pdu.code as pduCode,
                pdu.description as pduDescription,
                t.provider.code as regionCode,
                t.provider.description as regionDescription
            from Staff s
            join s.teams t
            join t.localAdminUnit ldu
            join ldu.probationDeliveryUnit pdu
            where t.provider.code = :providerCode and pdu.selectable = true and ldu.selectable = true 
            and t.endDate is null and s.endDate is null
        """
    )
    fun findRegionMembers(providerCode: String): List<RegionMember>
}

fun StaffRepository.getAllByCodeIn(codes: List<String>) =
    codes.toSet().let { codes -> findAllByCodeIn(codes).associateBy { it.code }.reportMissing(codes) }

interface RegionMember {
    val staffCode: String
    val forename: String
    val surname: String
    val teamCode: String
    val teamDescription: String
    val pduCode: String
    val pduDescription: String
    val regionCode: String
    val regionDescription: String
}