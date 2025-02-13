package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "team")
class Team(

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    val description: String,
    val telephone: String?,
    val emailAddress: String?,

    @ManyToMany
    @JoinTable(
        name = "team_office_location",
        joinColumns = [JoinColumn(name = "team_id")],
        inverseJoinColumns = [JoinColumn(name = "office_location_id")]
    )
    @SQLRestriction("end_date is null or end_date > current_date")
    val addresses: Set<OfficeLocation>,

    val startDate: LocalDate,
    val endDate: LocalDate?,

    @Id
    @Column(name = "team_id")
    val id: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Team

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

@Immutable
@Entity
@Table(name = "office_location")
class OfficeLocation(

    val description: String,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val district: String?,
    val townCity: String?,
    val county: String?,
    val postcode: String?,
    val telephoneNumber: String?,
    val startDate: LocalDate,
    val endDate: LocalDate?,

    @Id
    @Column(name = "office_location_id")
    val id: Long
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Team

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}