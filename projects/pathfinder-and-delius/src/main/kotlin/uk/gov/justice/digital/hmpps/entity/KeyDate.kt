package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "custody")
@SQLRestriction("soft_deleted = 0")
class Custody(
    @Id
    @Column(name = "custody_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @OneToMany(mappedBy = "custody")
    val keyDates: List<KeyDate> = listOf(),

    @Column(name = "soft_deleted", columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false
)

@Entity
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

    @Column(name = "soft_deleted", columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false
)
