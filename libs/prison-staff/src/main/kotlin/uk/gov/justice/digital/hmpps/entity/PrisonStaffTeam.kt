package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.ZonedDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "staff_team")
@IdClass(StaffTeamId::class)
class PrisonStaffTeam(

    @Id
    @Column(name = "staff_id")
    val staffId: Long,

    @Id
    @Column(name = "team_id")
    val teamId: Long,

    @CreatedBy
    @Column(name = "created_by_user_id", updatable = false)
    var createdByUserId: Long = 0,

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    val lastModifiedUserId: Long = 0,

    @CreatedDate
    @Column(name = "created_datetime", updatable = false)
    val createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    val lastModifiedDate: ZonedDateTime = ZonedDateTime.now(),

    @Version
    @Column(name = "row_version")
    val version: Long = 0
)

data class StaffTeamId(val staffId: Long = 0, val teamId: Long = 0) : Serializable
