package uk.gov.justice.digital.hmpps.integrations.delius.court.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Immutable
@Table(name = "r_offence")
@Entity
class Offence(

    @Column(columnDefinition = "char(5)")
    val code: String,

    @Id
    @Column(name = "offence_id")
    val id: Long
)

interface OffenceRepository : JpaRepository<Offence, Long> {
    fun findByCode(code: String): Offence?
}

fun OffenceRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("Offence", "code", code)
