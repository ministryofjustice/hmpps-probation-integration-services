package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Version
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import java.io.Serializable
import java.time.ZonedDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity
@IdClass(StaffTeamId::class)
class StaffTeam(

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
    var lastModifiedUserId: Long = 0,

    @CreatedDate
    @Column(name = "created_datetime", updatable = false)
    var createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastModifiedDate: ZonedDateTime = ZonedDateTime.now(),

    @Version
    @Column(name = "row_version")
    var version: Long = 0
)

interface StaffTeamRepository : JpaRepository<StaffTeam, StaffTeamId>

data class StaffTeamId(val staffId: Long = 0, val teamId: Long = 0) : Serializable
