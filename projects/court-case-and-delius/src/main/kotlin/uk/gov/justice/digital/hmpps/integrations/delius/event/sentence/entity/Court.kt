package uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProbationAreaEntity
import java.time.ZonedDateTime

@Entity
@Table(name = "court")
@Immutable
class Court(
    @Id
    @Column(name = "court_id")
    val id: Long,

    @Column(columnDefinition = "char(6)")
    val code: String,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,

    val courtName: String?,

    val telephoneNumber: String?,

    @Column(name = "fax")
    val faxNumber: String?,

    val buildingName: String,

    val street: String?,

    val locality: String?,

    val town: String?,

    val county: String?,

    val postcode: String?,

    val country: String?,

    @Column(name = "court_type_id", updatable = false, insertable = false)
    val courtTypeId: Long,

    val createdDatetime: ZonedDateTime,

    val lastUpdatedDatetime: ZonedDateTime,

    @Column(name = "probation_area_id", updatable = false, insertable = false)
    val probationAreaId: Long,

    val secureEmailAddress: String?,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val probationArea: ProbationAreaEntity,

    @ManyToOne
    @JoinColumn(name = "court_type_id")
    val courtType: ReferenceData
)