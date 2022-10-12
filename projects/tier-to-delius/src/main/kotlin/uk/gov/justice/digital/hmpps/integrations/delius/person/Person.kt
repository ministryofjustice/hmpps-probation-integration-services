package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "offender")
class Person(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column
    var currentTier: Long? = null,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,

    @OneToMany(mappedBy = "person", fetch = FetchType.EAGER)
    @Where(clause = "active_flag = 1")
    val managers: MutableList<PersonManager> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column(nullable = false)
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(nullable = false, updatable = false)
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(nullable = false)
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),
)
