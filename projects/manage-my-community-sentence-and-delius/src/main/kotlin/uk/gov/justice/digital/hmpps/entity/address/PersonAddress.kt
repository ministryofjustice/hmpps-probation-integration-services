package uk.gov.justice.digital.hmpps.entity.address

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import java.time.LocalDate

@Entity
@Table(name = "offender_address")
@SQLRestriction("soft_deleted = 0 and (end_date is null or end_date > current_date)")
class PersonAddress(
    @Id
    @Column(name = "offender_address_id")
    val id: Long,
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,
    @ManyToOne
    @JoinColumn(name = "address_status_id")
    val status: ReferenceData,
    val buildingName: String? = null,
    val addressNumber: String? = null,
    val streetName: String? = null,
    val district: String? = null,
    @Column(name = "town_city")
    val town: String? = null,
    val county: String? = null,
    val postcode: String? = null,
    @Convert(converter = YesNoConverter::class)
    val noFixedAbode: Boolean? = false,
    val endDate: LocalDate? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
) {
    companion object {
        const val MAIN_ADDRESS_STATUS = "M"
    }
}
