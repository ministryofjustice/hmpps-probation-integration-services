package uk.gov.justice.digital.hmpps.integrations.delius.provider

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "probation_area")
class Provider(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,
    @Column(name = "code", columnDefinition = "char(3)")
    val code: String,
    val description: String,
)
