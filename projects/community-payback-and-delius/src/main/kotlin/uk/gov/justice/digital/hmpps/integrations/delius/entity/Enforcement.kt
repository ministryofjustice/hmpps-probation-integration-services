package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "enforcement")
@EntityListeners(AuditingEntityListener::class)
class Enforcement(
    @Id
    @SequenceGenerator(name = "enforcement_id_seq", sequenceName = "enforcement_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "enforcement_id_seq")
    @Column(name = "enforcement_id")
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "contact_id")
    val contact: Contact,

    @ManyToOne
    @JoinColumn(name = "enforcement_action_id")
    val enforcementAction: EnforcementAction? = null,

    @Column(name = "response_date")
    val responseDate: LocalDate? = null,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "action_taken_date")
    val actionTakenDate: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "action_taken_time")
    val actionTakenTime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "partition_area_id")
    val partitionAreaId: Long = 0,

    @CreatedDate
    @Column(name = "created_datetime")
    var createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    @Column(name = "created_by_user_id")
    var createdByUserId: Long = 0,

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastUpdatedUserId: Long = 0

)

interface EnforcementRepository : JpaRepository<Enforcement, Long>