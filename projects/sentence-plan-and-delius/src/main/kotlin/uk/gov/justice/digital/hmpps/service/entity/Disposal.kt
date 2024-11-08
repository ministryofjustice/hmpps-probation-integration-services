package uk.gov.justice.digital.hmpps.service.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val type: DisposalType,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @OneToOne(mappedBy = "disposal")
    var custody: Custody? = null,

    @Column(name = "disposal_date")
    val startDate: LocalDate,

    @Column(name = "notional_end_date")
    val endDate: LocalDate,

    @Column(name = "entered_notional_end_date")
    val enteredSentenceEndDate: LocalDate? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
) {
    fun expectedEndDate() = enteredSentenceEndDate ?: endDate
}

@Entity
@Immutable
@Table(name = "r_disposal_type")
data class DisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,

    @Column(name = "description")
    val description: String,

    val sentenceType: String
) {
    fun isCustodial() = sentenceType in listOf("NC", "SC")
}

interface DisposalRepository : JpaRepository<Disposal, Long> {

    @Query(
        """
        select d from Disposal d
        join d.event e
        where e.person.crn = :crn
    """
    )
    fun findActiveSentences(crn: String): List<Disposal>
}

