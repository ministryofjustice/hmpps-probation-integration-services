package uk.gov.justice.digital.hmpps.integrations.delius.person

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea

@Entity
@Immutable
@Table(name = "offender_manager")
class PersonManager(
    @Id
    @Column(name = "offender_manager_id", nullable = false)
    val id: Long,
    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,
    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    val probationArea: ProbationArea,
    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,
    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false,
)
