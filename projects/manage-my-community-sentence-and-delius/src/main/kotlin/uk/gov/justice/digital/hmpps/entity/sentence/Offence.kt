package uk.gov.justice.digital.hmpps.entity.sentence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Table(name = "main_offence")
@Immutable
class MainOffence (
    @Id
    @Column(name = "main_offence_id")
    val id: Long = 0,

    @OneToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,
)

@Entity
@Table(name = "r_offence")
@Immutable
class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long = 0,

    @Column(name = "sub_category_code")
    val code: String,

    @Column(name = "sub_category_description")
    val description: String,
)

@Entity
@Table(name = "additional_offence")
@Immutable
class AdditionalOffence(
    @Id
    @Column(name = "additional_offence_id")
    val id: Long = 0,

    @OneToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,

    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal? = null,
)