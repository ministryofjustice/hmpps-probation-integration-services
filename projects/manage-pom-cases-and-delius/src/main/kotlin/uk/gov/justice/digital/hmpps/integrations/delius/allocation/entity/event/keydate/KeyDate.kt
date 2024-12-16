package uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDate.TypeCode.HANDOVER_DATE
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDate.TypeCode.HANDOVER_START_DATE
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("soft_deleted = 0")
@SequenceGenerator(name = "key_date_id_seq", sequenceName = "key_date_id_seq", allocationSize = 1)
class KeyDate(

    @Column(name = "custody_id")
    val custodyId: Long,

    @ManyToOne
    @JoinColumn(name = "key_date_type_id")
    val type: ReferenceData,

    @Column(name = "key_date")
    var date: LocalDate,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "key_date_id_seq")
    @Column(name = "key_date_id")
    val id: Long = 0
) {
    val partitionAreaId = 0

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    var softDeleted: Boolean = false

    @Column(name = "row_version")
    @Version
    var version: Long = 0

    @Column(name = "created_datetime")
    @CreatedDate
    var createdDateTime: ZonedDateTime = ZonedDateTime.now()

    @Column(name = "last_updated_datetime")
    @LastModifiedDate
    var lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now()

    @Column(name = "created_by_user_id")
    @CreatedBy
    var createdUserId: Long = 0

    @Column(name = "last_updated_user_id")
    @LastModifiedBy
    var lastModifiedUserId: Long = 0

    enum class TypeCode(val value: String) {
        HANDOVER_START_DATE("POM1"),
        HANDOVER_DATE("POM2")
    }
}

interface KeyDateRepository : JpaRepository<KeyDate, Long> {
    fun findAllByCustodyIdAndTypeCodeIn(custodyId: Long, types: List<String>): List<KeyDate>
}

fun KeyDateRepository.findHandoverDates(custodyId: Long) =
    findAllByCustodyIdAndTypeCodeIn(custodyId, listOf(HANDOVER_START_DATE.value, HANDOVER_DATE.value))
