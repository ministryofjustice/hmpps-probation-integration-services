package uk.gov.justice.digital.hmpps.controller.casedetails.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.common.entity.ReferenceData

@Immutable
@Entity
@Table(name = "offender_address")
@SQLRestriction("soft_deleted = 0")
class CaseAddress(
    @Id
    @Column(name = "offender_address_id")
    val id: Long,
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: CaseEntity,
    @Column(name = "building_name")
    val buildingName: String?,
    @Column(name = "address_number")
    val addressNumber: String?,
    @Column(name = "street_name")
    val streetName: String?,
    @Column(name = "town_city")
    val town: String?,
    val district: String?,
    val county: String?,
    val postcode: String?,
    val telephoneNumber: String?,
    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
    @ManyToOne
    @JoinColumn(name = "address_status_id")
    val status: ReferenceData
)
