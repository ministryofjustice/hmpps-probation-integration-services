package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "borough")
class ProbationDeliveryUnit(
    @Id
    @Column(name = "borough_id")
    val id: Long,
    val code: String,
    val description: String,
)
