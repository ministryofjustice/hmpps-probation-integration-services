package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "office_location")
@SQLRestriction("end_date is null or end_date > current_date")
class OfficeLocation(
    @Column(columnDefinition = "char(7)")
    val code: String,
    val description: String,
    val endDate: LocalDate?,

    @Id
    @Column(name = "office_location_id")
    val id: Long
)