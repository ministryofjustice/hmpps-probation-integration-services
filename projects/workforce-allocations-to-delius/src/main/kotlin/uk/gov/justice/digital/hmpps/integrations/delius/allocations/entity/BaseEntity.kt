package uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity

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
    @Column(name = "soft_deleted", columnDefinition = "NUMBER", nullable = false)
    var softDeleted: Boolean = false

    @Column(name = "row_version", nullable = false)
    @Version
    var version: Long = 0

    @Column(name = "created_datetime", nullable = false)
    @CreatedDate
    var createdDateTime: ZonedDateTime? = null

    @Column(name = "last_updated_datetime", nullable = false)
    @LastModifiedDate
    var lastModifiedDateTime: ZonedDateTime? = null

    @Column(name = "created_by_user_id", nullable = false)
    @CreatedBy
    var createdUserId: Long? = null

    @Column(name = "last_updated_user_id", nullable = false)
    @LastModifiedBy
    var lastModifiedUserId: Long? = null
}
