package uk.gov.justice.digital.hmpps.integrations.common.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction

@Immutable
@Entity
@Table(name = "address")
@SQLRestriction("soft_deleted = 0")
class AddressEntity(
    @Id
    @Column(name = "address_id")
    val id: Long,
    val buildingName: String?,
    val addressNumber: String?,
    val streetName: String?,
    val district: String?,
    @Column(name = "town_city")
    val town: String?,
    val county: String?,
    val postcode: String?,
    val telephoneNumber: String?,
    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false
)
