package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter

@Entity
@Immutable
class District(
    @Id
    @Column(name = "district_id")
    val id: Long,

    @Column
    val code: String,

    @Column
    val description: String,

    @Column
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,

    @ManyToOne
    @JoinColumn(name = "borough_id")
    val borough: Borough,

    @OneToMany(mappedBy = "district")
    val teams: List<Team> = listOf()
)
