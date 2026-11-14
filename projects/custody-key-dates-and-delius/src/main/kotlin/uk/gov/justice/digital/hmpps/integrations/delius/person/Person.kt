package uk.gov.justice.digital.hmpps.integrations.delius.person

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.Registration

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
    val nomsId: String?,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @OneToMany(mappedBy = "person")
    @SQLRestriction("register_type_id in (select r_register_type.register_type_id from r_register_type where r_register_type.code = 'PMEX')")
    val progressionModelExclusionRegistrations: List<Registration> = emptyList()
)
