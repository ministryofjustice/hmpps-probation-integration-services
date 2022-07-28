package uk.gov.justice.digital.hmpps.integrations.delius.managers

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.MappedSuperclass
import javax.persistence.Version

@MappedSuperclass
abstract class BaseEntity {

  @Column(name = "SOFT_DELETED", columnDefinition = "NUMBER", nullable = false)
  var softDeleted: Boolean = false

  @Column(name = "ROW_VERSION", nullable = false)
  @Version
  var rowVersion: Long? = null

  @Column(name = "CREATED_DATETIME", nullable = false)
  @CreatedDate
  var createdDateTime: ZonedDateTime? = null

  @Column(name = "LAST_UPDATED_DATETIME", nullable = false)
  @LastModifiedDate
  var lastUpdatedDateTime: ZonedDateTime? = null

  @Column(name = "CREATED_BY_USER_ID", nullable = false)
  @CreatedBy
  var createdByUserId: Long? = null

  @Column(name = "LAST_UPDATED_USER_ID", nullable = false)
  @LastModifiedBy
  var lastUpdatedUserId: Long? = null
}
