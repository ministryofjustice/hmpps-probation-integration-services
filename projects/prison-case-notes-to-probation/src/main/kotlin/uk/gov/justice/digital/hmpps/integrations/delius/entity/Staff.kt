package uk.gov.justice.digital.hmpps.integrations.delius.entity

import java.time.LocalDate
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Version

@Entity(name = "Staff")
class Staff(

    @Id
    @Column(name = "staff_id")
    @SequenceGenerator(name = "idGenerator", sequenceName = "staff_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idGenerator")
    val id: Long = 0,

    @Column(name = "forename")
    val forename: String,

    @Column(name = "surname")
    val surname: String,

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    @Column(name = "probation_area_id")
    val probationAreaId: Long,

    @Column(name = "private", columnDefinition = "NUMBER", nullable = false)
    var privateStaff: Boolean = false,

    @Column(name = "start_date", updatable = false)
    val startDate: LocalDate = LocalDate.now(),

    @Column(name = "last_updated_datetime")
    val lastModifiedDate: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "last_updated_user_id")
    val lastModifiedUserId: Long,

    @Column(name = "created_by_user_id", updatable = false)
    val createdByUserId: Long,

    @Column(name = "created_datetime", updatable = false)
    val createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @Version
    @Column(name = "row_version")
    val version: Long = 0,
)
