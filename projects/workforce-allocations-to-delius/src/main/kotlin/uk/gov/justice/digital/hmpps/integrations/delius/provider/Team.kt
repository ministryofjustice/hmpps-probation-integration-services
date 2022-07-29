package uk.gov.justice.digital.hmpps.integrations.delius.provider

import org.hibernate.annotations.Immutable
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

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
)
