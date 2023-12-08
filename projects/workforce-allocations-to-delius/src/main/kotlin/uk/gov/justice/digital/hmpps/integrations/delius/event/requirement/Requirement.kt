package uk.gov.justice.digital.hmpps.integrations.delius.event.requirement

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

@Immutable
@Entity
@Table(name = "rqmnt")
class Requirement(
    @Id
    @Column(name = "rqmnt_id", nullable = false)
    val id: Long,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "disposal_id", nullable = false)
    val disposal: Disposal,
    @ManyToOne
    @JoinColumn(name = "rqmnt_type_main_category_id")
    val mainCategory: RequirementMainCategory?,
    @ManyToOne
    @JoinColumn(name = "ad_rqmnt_type_main_category_id")
    val additionalMainCategory: RequirementAdditionalMainCategory?,
    @ManyToOne
    @JoinColumn(name = "rqmnt_type_sub_category_id")
    val subCategory: ReferenceData?,
    @Column(name = "active_flag", columnDefinition = "NUMBER", nullable = false)
    val active: Boolean = true,
    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,
)

@Immutable
@Entity
@Table(name = "r_rqmnt_type_main_category")
class RequirementMainCategory(
    @Id
    @Column(name = "rqmnt_type_main_category_id", nullable = false)
    val id: Long,
    val code: String,
    val description: String,
)

@Immutable
@Entity
@Table(name = "r_ad_rqmnt_type_main_category")
class RequirementAdditionalMainCategory(
    @Id
    @Column(name = "ad_rqmnt_type_main_category_id", nullable = false)
    val id: Long,
    val code: String,
    val description: String,
)
