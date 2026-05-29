package uk.gov.justice.digital.hmpps.entity.appointment

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.LastModifiedDate
import uk.gov.justice.digital.hmpps.entity.address.OfficeLocation
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointment
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

@Entity
@SQLRestriction("soft_deleted = 0 and (sensitive is null or sensitive <> 'Y')")
class Contact(
    @Id
    @Column(name = "contact_id")
    val id: Long,
    @Column(name = "offender_id")
    val personId: Long,
    @Column(name = "contact_date")
    val date: LocalDate,
    @Column(name = "contact_start_time")
    val startTime: LocalTime,
    @Column(name = "contact_end_time")
    val endTime: LocalTime? = null,
    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,
    val description: String? = null,
    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    val outcome: ContactOutcome? = null,
    @Column(name = "nsi_id")
    val nsiId: Long? = null,
    @Convert(converter = YesNoConverter::class)
    val rarActivity: Boolean? = null,
    @Convert(converter = YesNoConverter::class)
    val attended: Boolean? = null,
    @Convert(converter = YesNoConverter::class)
    val complied: Boolean? = null,
    @ManyToOne
    @JoinColumn(name = "office_location_id")
    val location: OfficeLocation? = null,
    @ManyToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff,
    @Convert(converter = YesNoConverter::class)
    val sensitive: Boolean? = false,
    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastUpdatedDatetime: ZonedDateTime,
    @OneToOne(mappedBy = "contact")
    var unpaidWorkAppointment: UnpaidWorkAppointment? = null
)
