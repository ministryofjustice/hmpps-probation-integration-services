package uk.gov.justice.digital.hmpps.integrations.delius.provider

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

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
