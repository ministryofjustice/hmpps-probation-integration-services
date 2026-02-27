package uk.gov.justice.digital.hmpps.entity.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable

@Entity
@Immutable
class OfficeLocation(
    @Id
    @Column(name = "office_location_id")
    val id: Long,

    val description: String,
)
