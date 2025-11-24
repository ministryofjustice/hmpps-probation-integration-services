package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "offender_religion_history")
class ReligionHistory(
    @Id
    @Column(name = "offender_religion_history_id")
    val id: Long = 0,

    @Column(name = "offender_id")
    val offenderId: Long = 0,

    @Column(name = "religion_id")
    val code: Long = 0,

    @Column(name = "religion_description")
    val description: String = "",

    @Column(name = "start_date")
    val startDate: LocalDate = LocalDate.MIN,

    @Column(name = "end_date")
    val endDate: LocalDate = LocalDate.MIN
)