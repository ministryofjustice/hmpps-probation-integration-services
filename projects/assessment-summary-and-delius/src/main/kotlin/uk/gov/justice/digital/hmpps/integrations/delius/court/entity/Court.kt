package uk.gov.justice.digital.hmpps.integrations.delius.court.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Immutable
@Entity
class Court(
    val code: String,
    @Id
    @Column(name = "court_id")
    val id: Long
)

interface CourtRepository : JpaRepository<Court, Long> {
    fun findByCode(code: String): Court?
}

fun CourtRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("Court", "code", code)
