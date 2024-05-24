package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity(name = "PrisonStaff")
@Table(name = "staff")
class PrisonStaff(

    @Id
    @Column(name = "staff_id")
    @SequenceGenerator(name = "staff_id_seq", sequenceName = "staff_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "staff_id_seq")
    val id: Long = 0,

    @Column(name = "forename")
    val forename: String,

    @Column(name = "surname")
    val surname: String,

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    @Column(name = "probation_area_id")
    val probationAreaId: Long,

    @Column(name = "start_date", updatable = false)
    val startDate: ZonedDateTime = ZonedDateTime.now(),

    @CreatedDate
    @Column(name = "created_datetime", updatable = false)
    val createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    val lastModifiedDate: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "private", columnDefinition = "NUMBER", nullable = false)
    var privateStaff: Boolean = false,

    @CreatedBy
    @Column(name = "created_by_user_id", updatable = false)
    var createdByUserId: Long = 0,

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastModifiedUserId: Long = 0,

    @Version
    @Column(name = "row_version")
    val version: Long = 0
) {
    fun isUnallocated() = code.endsWith("U")
}
