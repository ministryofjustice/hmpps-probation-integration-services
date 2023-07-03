package uk.gov.justice.digital.hmpps.controller.personaldetails.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where

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

    @OneToMany(mappedBy = "person", fetch = FetchType.EAGER)
    @Where(clause = "soft_deleted = 0")
    val personalCircumstances: List<PersonalCircumstanceEntity>,

    @OneToMany(mappedBy = "person", fetch = FetchType.EAGER)
    @Where(clause = "soft_deleted = 0")
    val personalContacts: List<PersonalContactEntity>
)
