package uk.gov.justice.digital.hmpps.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.config.CsvMapperConfig.csvMapper
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.InitialAllocation
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.InitialAllocationRepository
import java.io.OutputStream
import kotlin.streams.asSequence

@Service
class InitialAllocationService(private val initialAllocationRepository: InitialAllocationRepository) {
    @Transactional
    fun writeInitialAllocations(outputStream: OutputStream) {
        val results = initialAllocationRepository.findAllInitialAllocations().asSequence()
        csvMapper
            .writer(csvMapper.schemaFor(InitialAllocation::class.java).withHeader())
            .writeValues(outputStream.bufferedWriter())
            .use { writer -> results.forEach(writer::write) }
    }
}
