package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "office_location")
class OfficeLocation(
    @Id
    @Column(name = "office_location_id")
    val id: Long,

    @Column(name = "description")
    val description: String
)

