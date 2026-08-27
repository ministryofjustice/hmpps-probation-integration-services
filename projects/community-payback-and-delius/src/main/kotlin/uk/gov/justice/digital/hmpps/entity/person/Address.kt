package uk.gov.justice.digital.hmpps.entity.person

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import java.time.ZonedDateTime

@Entity
@Table(name = "address")
@SequenceGenerator(name = "address_id_generator", sequenceName = "address_id_seq", allocationSize = 1)
@SQLRestriction("soft_deleted = 0")
class Address(
    @Id
    @GeneratedId(generator = "address_id_generator")
    @Column(name = "address_id")
    val id: Long = 0,

    @Version
    @Column(name = "row_version")
    val rowVersion: Long = 0,

    val buildingName: String?,

    val addressNumber: String?,

    val streetName: String?,

    @Column(name = "town_city")
    val town: String?,

    val county: String?,

    val postcode: String?,

    val telephoneNumber: String?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @CreatedDate
    @Column(name = "created_datetime")
    var createdDatetime: ZonedDateTime? = ZonedDateTime.now(),

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastUpdatedDatetime: ZonedDateTime? = ZonedDateTime.now(),

    @CreatedBy
    @Column(name = "created_by_user_id")
    var createdByUserId: Long? = null,

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastUpdatedUserId: Long? = null,
)