package uk.gov.justice.digital.hmpps.integrations.delius.provider

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "team")
@SQLRestriction("end_date is null or end_date > current_date")
class TeamWithDistrict(
    @Id
    @Column(name = "team_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    val description: String,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val district: District,

    @Column(name = "end_date")
    val endDate: ZonedDateTime? = null,

    @ManyToMany(mappedBy = "teams")
    val staff: List<StaffWithTeams> = listOf()
)

@Immutable
@Entity
@Table(name = "district")
class District(
    @Id
    @Column(name = "district_id")
    val id: Long,

    @Column(name = "code")
    val code: String,

    @Column(name = "description")
    val description: String,

    @ManyToOne
    @JoinColumn(name = "borough_id")
    val borough: Borough,
)

@Immutable
@Entity
@Table(name = "borough")
class Borough(
    @Id
    @Column(name = "borough_id")
    val id: Long,

    @Column(name = "code")
    val code: String,

    @Column(name = "description")
    val description: String,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val probationArea: Provider,
)
