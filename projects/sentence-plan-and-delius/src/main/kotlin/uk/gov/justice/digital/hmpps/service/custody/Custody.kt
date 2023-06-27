package uk.gov.justice.digital.hmpps.service.custody

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import uk.gov.justice.digital.hmpps.service.ReferenceData
import uk.gov.justice.digital.hmpps.service.event.Disposal

@Immutable
@Entity
@Where(clause = "soft_deleted = 0")
class Custody(
    @Id
    @Column(name = "custody_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "custodial_status_id")
    var status: ReferenceData,

    @OneToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)
