package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Version
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.ZonedDateTime

@MappedSuperclass
abstract class AuditableEntity {
    val partitionAreaId: Long = 0

    @Version
    @Column(name = "row_version")
    var version: Long = 0

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now()

    @CreatedBy
    var createdByUserId: Long = 0

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0
}
