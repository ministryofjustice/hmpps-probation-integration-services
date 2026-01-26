package uk.gov.justice.digital.hmpps.entity.unpaidwork

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Table(name = "upw_day")
@Immutable
class UnpaidWorkDay(
    @Id
    @Column(name = "upw_day_id")
    val id: Long,

    val weekDay: String
)