package uk.gov.justice.digital.hmpps.integrations.delius.custody

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Version
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.ZonedDateTime

@MappedSuperclass
abstract class BaseEntity {
    val partitionAreaId = 0

    @Column(name = "soft_deleted", columnDefinition = "NUMBER", nullable = false)
    var softDeleted: Boolean = false

    @Column(name = "row_version", nullable = false)
    @Version
    var version: Long = 0

    @Column(name = "created_datetime", nullable = false)
    @CreatedDate
    var createdDateTime: ZonedDateTime = ZonedDateTime.now()

    @Column(name = "last_updated_datetime", nullable = false)
    @LastModifiedDate
    var lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now()

    @Column(name = "created_by_user_id")
    @CreatedBy
    var createdUserId: Long = 0

    @Column(name = "last_updated_user_id")
    @LastModifiedBy
    var lastModifiedUserId: Long = 0
}
