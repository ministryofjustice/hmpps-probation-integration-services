package uk.gov.justice.digital.hmpps.integrations.delius.person

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "offender")
class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long,
    @Column(columnDefinition = "char(7)")
    val crn: String,
    @Column(name = "noms_number", columnDefinition = "char(7)")
    val nomsId: String? = null,
    @Column(name = "first_name", length = 35)
    val forename: String,
    @Column(name = "second_name", length = 35)
    val secondName: String? = null,
    @Column(name = "third_name", length = 35)
    val thirdName: String? = null,
    @Column(name = "surname", length = 35)
    val surname: String,
    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,
    val exclusionMessage: String? = null,
    val restrictionMessage: String? = null,
)
