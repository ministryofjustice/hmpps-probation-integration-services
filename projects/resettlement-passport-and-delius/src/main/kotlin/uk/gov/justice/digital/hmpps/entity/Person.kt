package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val noms: String?,

    val firstName: String,
    val surname: String,
    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    val telephoneNumber: String?,
    val mobileNumber: String?,
    @Column(name = "e_mail_address")
    val emailAddress: String?,

    val softDeleted: Boolean = false
)