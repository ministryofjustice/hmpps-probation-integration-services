package uk.gov.justice.digital.hmpps.entity.sentence.requirement

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.entity.ReferenceData

@Entity
@Immutable
@Table(name = "r_rqmnt_type_main_category")
class RequirementMainCategory(
    @Id
    @Column(name = "rqmnt_type_main_category_id")
    val id: Long,
    val code: String,
    val description: String,
    @ManyToOne
    @JoinColumn(name = "units_id")
    val lengthUnits: ReferenceData,
)