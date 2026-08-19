package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "staff")
class Staff(
    @Id
    @Column(name = "staff_id")
    val id: Long,
    @Column(name = "forename")
    val forename: String,
    @Column(name = "forename2")
    val middleName: String? = null,
    @Column(name = "surname")
    val surname: String,
    @Column(name = "officer_code")
    val code: String,
    @ManyToOne
    @JoinColumn(name = "staff_grade_id")
    val jobRole: ReferenceData? = null,
    @OneToOne(mappedBy = "staff")
    val user: StaffUser? = null,
)

