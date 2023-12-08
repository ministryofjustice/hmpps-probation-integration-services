package uk.gov.justice.digital.hmpps.service.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "staff")
class Staff(
    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,
    val forename: String,
    val surname: String,
    @Column(name = "forename2")
    val middleName: String? = null,
    @Id
    @Column(name = "staff_id")
    val id: Long,
) {
    fun isUnallocated() = code.endsWith("U")
}
