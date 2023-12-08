package uk.gov.justice.digital.hmpps.integrations.delius.probationarea.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinColumns
import jakarta.persistence.OneToOne
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.Institution

@Entity
@Immutable
class ProbationArea(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,
    @Column(columnDefinition = "char(3)")
    val code: String,
    @Column
    val description: String,
    @OneToOne
    @JoinColumns(
        JoinColumn(name = "institution_id", referencedColumnName = "institution_id"),
        JoinColumn(name = "establishment", referencedColumnName = "establishment"),
    )
    var institution: Institution? = null,
)
