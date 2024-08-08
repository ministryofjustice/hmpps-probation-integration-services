package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "disposal_date")
    val startDate: LocalDate,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean
)

interface DisposalRepository : JpaRepository<Disposal, Long> {
    fun findByPersonId(personId: Long): List<Disposal>
}
