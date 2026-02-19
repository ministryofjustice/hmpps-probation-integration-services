package uk.gov.justice.digital.hmpps.utils

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.JpaSort

object Extensions {
    inline fun <K, reified V> Map<K, V>.reportMissing(required: Set<K>) = also {
        val missing = required - keys
        require(missing.isEmpty()) { "Invalid ${V::class.simpleName}: $missing" }
    }

    fun Pageable.mapSorts(vararg pairs: Pair<String, String>): Pageable {
        val mapping = pairs.toMap()
        return PageRequest.of(pageNumber, pageSize, sort.map { order ->
            val property = requireNotNull(mapping[order.property]) { "Unsupported sort: ${order.property}" }
            JpaSort.unsafe(order.direction, property)
        }.fold(Sort.unsorted()) { acc, sort -> if (acc.isUnsorted) sort else acc.and(sort) })
    }
}