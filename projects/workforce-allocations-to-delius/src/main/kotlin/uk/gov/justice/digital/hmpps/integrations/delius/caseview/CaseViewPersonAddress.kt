package uk.gov.justice.digital.hmpps.integrations.delius.caseview

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "offender_address")
@SQLRestriction("soft_deleted = 0")
class CaseViewPersonAddress(
    @Id
    @Column(name = "offender_address_id")
    val id: Long,
    @Column(name = "offender_id")
    val personId: Long,
    @ManyToOne
    @JoinColumn(name = "address_type_id")
    val type: ReferenceData?,
    @ManyToOne
    @JoinColumn(name = "address_status_id")
    val status: ReferenceData,
    @Column(name = "building_name")
    val buildingName: String?,
    @Column(name = "address_number")
    val addressNumber: String?,
    @Column(name = "street_name")
    val streetName: String?,
    @Column(name = "town_city")
    val town: String?,
    val county: String?,
    val postcode: String?,
    @Convert(converter = YesNoConverter::class)
    val noFixedAbode: Boolean? = false,
    @Convert(converter = YesNoConverter::class)
    val typeVerified: Boolean? = false,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)
