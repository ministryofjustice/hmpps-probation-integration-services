package uk.gov.justice.digital.hmpps.integrations.delius.entity

import java.io.Serializable
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.Version

@Entity(name = "StaffTeam")
@IdClass(StaffTeamId::class)
class StaffTeam(

    @Id
    @Column(name = "staff_id")
    private var staffId: Long,

    @Id
    @Column(name = "team_id")
    private var teamId: Long,

    @Column(name = "created_by_user_id", updatable = false)
    val createdByUserId: Long,

    @Column(name = "last_updated_user_id")
    val lastModifiedUserId: Long,

    @Column(name = "last_updated_datetime")
    val lastModifiedDate: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "created_datetime", updatable = false)
    val createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @Version
    @Column(name = "row_version")
    val version: Long = 0,
)

class StaffTeamId(private val staffId: Long, private val teamId: Long) : Serializable

