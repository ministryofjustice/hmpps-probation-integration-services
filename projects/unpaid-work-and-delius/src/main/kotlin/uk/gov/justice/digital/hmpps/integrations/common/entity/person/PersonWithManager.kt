package uk.gov.justice.digital.hmpps.integrations.common.entity.person

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

@Immutable
@Entity
@Table(name = "offender")
class PersonWithManager(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "first_name", length = 35)
    val forename: String,

    @Column(name = "surname", length = 35)
    val surname: String,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @OneToMany(mappedBy = "person")
    @SQLRestriction("active_flag = 1")
    val managers: List<PersonManager> = listOf()

)
