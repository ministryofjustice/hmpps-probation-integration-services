package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class ReferenceData(

    @Column(name = "code_value")
    val code: String,

    @Column(name = "code_description")
    val description: String,

    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long
)

enum class Category(val number: Int) { X9(0), M1(1), M2(2), M3(3), M4(4) }
enum class Level(val number: Int) { M0(0), M1(1), M2(2), M3(3) }
