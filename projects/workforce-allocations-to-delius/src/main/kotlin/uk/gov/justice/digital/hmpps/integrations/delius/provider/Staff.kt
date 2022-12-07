package uk.gov.justice.digital.hmpps.integrations.delius.provider

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable
import java.time.ZonedDateTime

@Immutable
@Entity
class Staff(

    @Id
    @Column(name = "staff_id")
    val id: Long = 0,

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    val forename: String,
    val surname: String,

    @Column(name = "FORENAME2", length = 35)
    val middleName: String?,

    @Column(name = "end_date")
    val endDate: ZonedDateTime? = null,
) {
    @Transient
    val displayName = listOfNotNull(forename, middleName, surname).joinToString(" ")
}
