package uk.gov.justice.digital.hmpps.controller.casedetails.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "alias")
@Where(clause = "soft_deleted = 0")
class AliasEntity(
    @Id
    @Column(name = "alias_id")
    val id: Long,
    @Column(name = "first_name", length = 35)
    val forename: String,
    @Column(name = "second_name", length = 35)
    val secondName: String? = null,
    @Column(name = "third_name", length = 35)
    val thirdName: String? = null,
    @Column(name = "surname", length = 35)
    val surname: String,
    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,
    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val case: CaseEntity,
    val softDeleted: Boolean = false,
)

