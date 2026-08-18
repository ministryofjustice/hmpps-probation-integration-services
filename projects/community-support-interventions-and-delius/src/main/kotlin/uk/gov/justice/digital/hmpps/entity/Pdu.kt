package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.NativeQuery

@Entity
@Table(name = "borough")
class Pdu(
    @Id
    @Column(name = "borough_id")
    val id: Long,

    val code: String,
    val description: String,
)

interface PduRepository : JpaRepository<Pdu, Long> {
    @NativeQuery(
        """
            select b.borough_id,
                   b.code,
                   b.description
            from team t
            join  district d on t.district_id = d.district_id
            join  borough b on d.borough_id = b.borough_id
            where t.code = :code
        """
    )
    fun findByTeamCode(code: String): Pdu?
}