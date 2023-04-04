package uk.gov.justice.digital.hmpps.integrations.delius.custody.keydate.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Version
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Immutable
@Where(clause = "soft_deleted = 0")
class KeyDate(
    val custodyId: Long,

    @ManyToOne
    @JoinColumn(name = "key_date_type_id")
    val type: ReferenceData,

    @Column(name = "key_date")
    var date: LocalDate,

    val partitionAreaId: Long = 0,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "key_date_id")
    val id: Long = 0,

    @Version
    @Column(name = "row_version")
    var version: Long = 0,

    @CreatedDate
    @Column(name = "created_datetime")
    var createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    @Column(name = "created_by_user_id")
    var createdUserId: Long = 0,

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastModifiedUserId: Long = 0
) {
    enum class TypeCode(val value: String) {
        AUTO_CONDITIONAL_RELEASE_DATE("ACR"),
        ROTL_END_DATE("ROTLD")
    }
}

interface KeyDateRepository : JpaRepository<KeyDate, Long> {
    @EntityGraph(attributePaths = ["type"])
    fun findByCustodyIdAndTypeCode(custodyId: Long, code: String): KeyDate?
}
