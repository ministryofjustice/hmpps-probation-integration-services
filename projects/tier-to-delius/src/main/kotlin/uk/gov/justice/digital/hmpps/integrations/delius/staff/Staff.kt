package uk.gov.justice.digital.hmpps.integrations.delius.staff

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
@Immutable
class Staff(

    @Id
    @Column(name = "staff_id")
    val id: Long = 0,

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,
)
