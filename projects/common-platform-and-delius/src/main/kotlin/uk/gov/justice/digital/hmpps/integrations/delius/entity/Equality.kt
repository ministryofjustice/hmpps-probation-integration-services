package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.ZonedDateTime

@Entity
@Table(name = "equality")
@SequenceGenerator(name = "equality_id_seq", sequenceName = "equality_id_seq", allocationSize = 1)
class Equality(
    @Id
    @Column(name = "equality_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "equality_id_seq")
    val id: Long? = null,

    @Column(name = "offender_id")
    val personId: Long,

    @CreatedBy
    var createdByUserId: Long = 0,

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column
    val softDeleted: Boolean,

    @Column
    @Version
    val rowVersion: Long = 0L
)
