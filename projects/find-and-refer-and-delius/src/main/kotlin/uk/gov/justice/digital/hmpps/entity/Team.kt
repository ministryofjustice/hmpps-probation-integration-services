package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import java.io.Serializable
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("end_date is null or end_date > current_date")
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val district: District,

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
    val addresses: List<OfficeLocation>,

    val startDate: LocalDate,
    val endDate: LocalDate?,

    )

@Embeddable
class TeamOfficeLinkId(
    @Column(name = "team_id")
    val teamId: Long,

    @ManyToOne
    @JoinColumn(name = "office_location_id")
    val officeLocation: OfficeLocation
) : Serializable

@Immutable
@Entity
@Table(name = "team_office_location")
class TeamOfficeLink(
    @Id
    val id: TeamOfficeLinkId
)
