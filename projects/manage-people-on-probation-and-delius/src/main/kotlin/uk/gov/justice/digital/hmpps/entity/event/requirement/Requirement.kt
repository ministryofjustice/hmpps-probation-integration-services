package uk.gov.justice.digital.hmpps.entity.event.requirement

import jakarta.persistence.*
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "rqmnt")
class Requirement(
    @Id
    @Column(name = "rqmnt_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_main_category_id")
    val mainCategory: RequirementMainCategory? = null,
) {
    fun isRar() = mainCategory?.code == "F"
}