package uk.gov.justice.digital.hmpps.entity.appointment

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.entity.address.OfficeLocation
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import java.time.LocalDate
import java.time.LocalTime

@Entity
@SQLRestriction("soft_deleted = 0 and (sensitive is null or sensitive <> 'Y')")
class Contact(
    @Id
    @Column(name = "contact_id")
    val id: Long,
    @Column(name = "offender_id")
    val personId: Long,
    @Column(name = "contact_date")
    var date: LocalDate,
    @Column(name = "contact_start_time")
    var startTime: LocalTime,
    @Column(name = "contact_end_time")
    var endTime: LocalTime? = null,
    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,
    var description: String? = null,
    @Convert(converter = YesNoConverter::class)
    var attended: Boolean? = null,
    @Convert(converter = YesNoConverter::class)
    var complied: Boolean? = null,
    @ManyToOne
    @JoinColumn(name = "office_location_id")
    var location: OfficeLocation? = null,
    @ManyToOne
    @JoinColumn(name = "staff_id")
    var staff: Staff,
    @Convert(converter = YesNoConverter::class)
    var sensitive: Boolean? = false,
    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)