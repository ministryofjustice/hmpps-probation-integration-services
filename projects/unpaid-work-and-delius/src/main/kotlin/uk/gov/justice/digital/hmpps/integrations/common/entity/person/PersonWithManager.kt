package uk.gov.justice.digital.hmpps.integrations.common.entity.person

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where

@Immutable
@Entity
@Table(name = "offender")
class PersonWithManager(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,

    @OneToMany(mappedBy = "person")
    @Where(clause = "active_flag = 1")
    val managers: List<PersonManager> = listOf(),

)
