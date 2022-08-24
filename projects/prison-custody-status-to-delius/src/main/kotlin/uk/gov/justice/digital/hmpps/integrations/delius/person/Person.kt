package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Immutable
@Entity
@Table(name = "offender")
class Person(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val nomsNumber: String,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,
)
