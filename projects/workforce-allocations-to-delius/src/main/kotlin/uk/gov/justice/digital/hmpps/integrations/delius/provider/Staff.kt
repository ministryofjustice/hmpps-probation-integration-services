package uk.gov.justice.digital.hmpps.integrations.delius.provider

import org.hibernate.annotations.Immutable
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Immutable
@Entity
class Staff(

    @Id
    @Column(name = "staff_id")
    val id: Long = 0,

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    forename: String,
    surname: String,

    @Column(name = "FORENAME2", length = 35)
    val middleName: String?,

    @Column(name = "end_date")
    val endDate: ZonedDateTime? = null,
) {
    val displayName = listOfNotNull(forename, middleName, surname).joinToString(" ")
}