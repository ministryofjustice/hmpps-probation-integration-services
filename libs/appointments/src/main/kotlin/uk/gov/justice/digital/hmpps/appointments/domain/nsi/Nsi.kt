package uk.gov.justice.digital.hmpps.appointments.domain.nsi

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.appointments.domain.event.Event
import uk.gov.justice.digital.hmpps.appointments.domain.event.component.Requirement
import uk.gov.justice.digital.hmpps.appointments.domain.person.Person
import java.time.ZonedDateTime

@Entity
@Table(name = "nsi")
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("soft_deleted = 0")
open class Nsi(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event?,

    @ManyToOne
    @JoinColumn(name = "rqmnt_id")
    val requirement: Requirement?,

    @Column(name = "rar_count")
    var rarCount: Long?,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    var active: Boolean,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nsi_id_generator")
    @Column(name = "nsi_id")
    val id: Long = 0,

    ) {
    @Version
    @Column(name = "row_version")
    val version: Long = 0

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now()

    @CreatedBy
    var createdByUserId: Long = 0

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0
}