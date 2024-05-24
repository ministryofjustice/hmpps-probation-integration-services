package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "staff")
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

    @Id
    @Column(name = "staff_id")
    @SequenceGenerator(name = "staff_id_seq", sequenceName = "staff_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "staff_id_seq")
    val id: Long = 0,
)

@Entity
@Immutable
@Table(name = "user_")
class StaffUser(

    @Column(name = "distinguished_name")
    val username: String,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff?,

    @Id
    @Column(name = "user_id")
    val id: Long
)

interface StaffRepository : JpaRepository<Staff, Long> {

    @Query(
        """
        select officer_code from staff
        where regexp_like(officer_code, ?1, 'i')
        order by officer_code desc
        fetch next 1 rows only
        """,
        nativeQuery = true
    )
    fun getLatestStaffReference(regex: String): String?
}
