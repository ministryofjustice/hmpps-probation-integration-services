package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Immutable
@Entity
@Table(name = "offender")
class Person(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,

    @OneToMany(mappedBy = "person")
    @Where(clause = "active_flag = 1")
    val managers: MutableList<PersonManager> = mutableListOf()
)
