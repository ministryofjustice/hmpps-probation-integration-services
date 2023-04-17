package uk.gov.justice.digital.hmpps.integrations.delius.recommendation.provider.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable

@Immutable
@Entity(name = "RecommendationStaff")
class Staff(

    @Id
    @Column(name = "staff_id")
    val id: Long,

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String
)
