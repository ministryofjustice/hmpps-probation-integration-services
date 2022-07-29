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

    val crn: String,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,
)
