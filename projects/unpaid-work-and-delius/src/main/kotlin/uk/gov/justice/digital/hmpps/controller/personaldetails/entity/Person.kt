package uk.gov.justice.digital.hmpps.controller.personaldetails.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

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
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @OneToMany(mappedBy = "person", fetch = FetchType.EAGER)
    @SQLRestriction("soft_deleted = 0")
    val personalCircumstances: List<PersonalCircumstanceEntity>,

    @OneToMany(mappedBy = "person", fetch = FetchType.EAGER)
    @SQLRestriction("soft_deleted = 0")
    val personalContacts: List<PersonalContactEntity>
)
