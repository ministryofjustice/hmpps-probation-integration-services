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
@Table(name = "borough")
class ProbationDeliveryUnit(
    @Id
    @Column(name = "borough_id")
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val probationArea: ProbationArea,
)
