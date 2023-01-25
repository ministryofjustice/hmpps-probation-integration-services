package uk.gov.justice.digital.hmpps.controller.personaldetails.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "personal_contact")
@Where(clause = "soft_deleted = 0 and end_date is null")
class PersonalContactEntity(
    @Id
    @Column(name = "personal_contact_id")
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    val relationship: String,

    @Column(name = "first_name")
    val forename: String,

    @Column(name = "other_names")
    val middleName: String?,

    @Column(name = "surname")
    val surname: String,

    @Column(name = "mobile_number")
    val mobileNumber: String?,

    @ManyToOne
    @JoinColumn(name = "address_id")
    val address: AddressEntity?,

    @Column(name = "start_date")
    val start: LocalDate? = null,

    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,
)
