package uk.gov.justice.digital.hmpps.integrations.delius.probationarea

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Immutable
@Table(name = "district")
class LocalAdminUnit(
    @Id
    @Column(name = "district_id")
    val id: Long = 0,

    @Column
    val description: String,

    @ManyToOne
    @JoinColumn(name = "borough_id")
    val probationDeliveryUnit: ProbationDeliveryUnit,
)
