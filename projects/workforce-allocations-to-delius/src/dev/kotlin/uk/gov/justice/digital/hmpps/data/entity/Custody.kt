package uk.gov.justice.digital.hmpps.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Version
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.Disposal
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class Custody(
    @Id
    @Column(name = "custody_id")
    val id: Long,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @ManyToOne
    @JoinColumn(name = "custodial_status_id")
    var status: ReferenceData,

    @OneToMany(mappedBy = "custody")
    val keyDates: List<KeyDate> = listOf(),

    @OneToOne
    @JoinColumn(name = "disposal_id", updatable = false)
    val disposal: Disposal,

    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false
)

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
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

    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false
)
