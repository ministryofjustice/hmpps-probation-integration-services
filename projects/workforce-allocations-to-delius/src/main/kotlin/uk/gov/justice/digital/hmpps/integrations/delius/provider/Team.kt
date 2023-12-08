package uk.gov.justice.digital.hmpps.integrations.delius.provider

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import org.hibernate.annotations.Immutable
import java.time.ZonedDateTime

@Immutable
@Entity
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,
    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,
    @Column(name = "probation_area_id")
    val providerId: Long,
    val description: String,
    @Column(name = "end_date")
    val endDate: ZonedDateTime? = null,
    @ManyToMany(mappedBy = "teams")
    val staff: List<StaffWithUser> = listOf(),
)
