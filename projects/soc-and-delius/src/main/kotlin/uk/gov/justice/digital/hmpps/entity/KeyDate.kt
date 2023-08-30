package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "custody")
class Custody(
    @Id
    @Column(name = "custody_id")
    val id: Long,

    @Column(name = "disposal_id")
    val disposalId: Long,

    @ManyToOne
    @JoinColumn(name = "custodial_status_id")
    val status: ReferenceData,

    @OneToMany(mappedBy = "custody")
    val keyDates: List<KeyDate> = listOf(),

    @Column(name = "soft_deleted", columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false
)

@Entity
@Where(clause = "soft_deleted = 0")
class KeyDate(

    @Id
    @Column(name = "key_date_id")
    val id: Long?,

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody? = null,

    @ManyToOne
    @JoinColumn(name = "key_date_type_id")
    val type: ReferenceData,

    @Column(name = "key_date")
    var date: LocalDate,

    @Column(name = "soft_deleted", columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false
)

interface CustodyRepository : JpaRepository<Custody, Long> {
    fun getCustodyByDisposalId(disposalId: Long): Custody?
}
