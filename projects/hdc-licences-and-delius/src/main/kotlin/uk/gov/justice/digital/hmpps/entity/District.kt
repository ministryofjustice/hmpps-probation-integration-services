package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter

@Entity
@Immutable
@SQLRestriction("selectable = 'Y' or code like '%UAT' or code like '%UNA' or code like '%IVA'")
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
    val teams: List<Team> = listOf(),
)
