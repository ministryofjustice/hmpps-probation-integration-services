package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_grade_id")
    val jobRole: ReferenceData? = null,
)

