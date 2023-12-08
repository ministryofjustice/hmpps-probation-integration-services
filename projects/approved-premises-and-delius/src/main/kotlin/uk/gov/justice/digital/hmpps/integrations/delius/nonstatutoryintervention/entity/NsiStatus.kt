package uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Entity
@Immutable
@Table(name = "r_nsi_status")
class NsiStatus(
    @Id
    @Column(name = "nsi_status_id")
    val id: Long,
    @Column(name = "code")
    val code: String,
)

enum class NsiStatusCode(val code: String) {
    IN_REFERRAL("AP01"),
    REFERRAL_ACCEPTED("AP02"),
    IN_RESIDENCE("AP03"),
}

interface NsiStatusRepository : JpaRepository<NsiStatus, Long> {
    fun findByCode(code: String): NsiStatus?
}

fun NsiStatusRepository.getByCode(code: String) = findByCode(code) ?: throw NotFoundException("NsiStatus", "code", code)
