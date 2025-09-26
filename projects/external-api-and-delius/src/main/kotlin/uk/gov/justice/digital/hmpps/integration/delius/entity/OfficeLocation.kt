package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "office_location")
class OfficeLocation(
    @Column(columnDefinition = "char(7)")
    val code: String,
    val description: String,
    val endDate: LocalDate?,

    @Id
    @Column(name = "office_location_id")
    val id: Long
)