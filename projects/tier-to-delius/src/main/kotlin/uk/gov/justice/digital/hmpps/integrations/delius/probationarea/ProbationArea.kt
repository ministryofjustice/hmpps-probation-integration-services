package uk.gov.justice.digital.hmpps.integrations.delius.probationarea

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
@Immutable
class ProbationArea(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @Column(columnDefinition = "char(3)")
    val code: String,
)
