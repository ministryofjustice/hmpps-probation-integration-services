package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime

@Entity
@Table(name = "staff")
@EntityListeners(AuditingEntityListener::class)
class Staff(

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    val forename: String,
    val surname: String,

    @Column(name = "forename2")
    val middleName: String? = null,

    @OneToOne(mappedBy = "staff")
    val user: StaffUser? = null,

    @Column(name = "probation_area_id")
    val probationAreaId: Long,

    @Column(name = "start_date", updatable = false)
    val startDate: ZonedDateTime = ZonedDateTime.now(),

    @Id
    @Column(name = "staff_id")
    @SequenceGenerator(name = "staff_id_seq", sequenceName = "staff_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "staff_id_seq")
    val id: Long = 0
) {
    fun isUnallocated() = code.endsWith("U")

    @Column(name = "private", columnDefinition = "NUMBER", nullable = false)
    var privateStaff: Boolean = false

    @CreatedDate
    @Column(name = "created_datetime", updatable = false)
    var createdDateTime: ZonedDateTime = ZonedDateTime.now()

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastModifiedDate: ZonedDateTime = ZonedDateTime.now()

    @CreatedBy
    @Column(name = "created_by_user_id", updatable = false)
    var createdByUserId: Long = 0

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastModifiedUserId: Long = 0

    @Version
    @Column(name = "row_version")
    var version: Long = 0
}

@Entity
@Immutable
@Table(name = "user_")
class StaffUser(

    @Column(name = "distinguished_name")
    val username: String,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,

    @Id
    @Column(name = "user_id")
    val id: Long
) {
    @Transient
    var email: String? = null
}
