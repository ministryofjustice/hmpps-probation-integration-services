package uk.gov.justice.digital.hmpps.entity.contact

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.event.requirement.Requirement
import uk.gov.justice.digital.hmpps.entity.user.OfficeLocation
import uk.gov.justice.digital.hmpps.entity.user.Staff
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class Contact(
    @Id
    @Column(name = "contact_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @ManyToOne
    @JoinColumn(name = "rqmnt_id")
    val requirement: Requirement? = null,

    @Column(name = "contact_date")
    val date: LocalDate,

    @Column(name = "contact_start_time")
    val startTime: LocalTime? = null,

    @Column(name = "contact_end_time")
    val endTime: LocalTime? = null,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "office_location_id")
    val location: OfficeLocation? = null,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    val outcome: ContactOutcome? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
) {
    fun startDateTime(): ZonedDateTime = date.atTime(startTime ?: LocalTime.MIN).atZone(EuropeLondon)
    fun endDateTime(): ZonedDateTime? = endTime?.let { date.atTime(endTime).atZone(EuropeLondon) }
}