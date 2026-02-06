package uk.gov.justice.digital.hmpps.jpa

import jakarta.persistence.SequenceGenerator
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.generator.GeneratorCreationContext
import org.hibernate.id.enhanced.SequenceStyleGenerator
import org.hibernate.persister.entity.EntityPersister
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.lang.reflect.Member
import java.util.*

@ExtendWith(MockitoExtension::class)
class OverridableIdGeneratorTest {
    @Mock
    private lateinit var context: GeneratorCreationContext

    @Mock
    private lateinit var session: SharedSessionContractImplementor

    @Mock
    private lateinit var persister: EntityPersister

    @SequenceGenerator(name = "test_gen", sequenceName = "test_seq", allocationSize = 50)
    class EntityWithClassAnnotation

    class EntityWithFieldAnnotation {
        @SequenceGenerator(name = "field_gen", sequenceName = "field_seq", initialValue = 100)
        val id: Long = 0
    }

    @Test
    fun `configures from class annotation`() {
        val annotation = GeneratedId("test_gen")
        val member = EntityWithClassAnnotation::class.java.getDeclaredConstructor() as Member
        val generator = OverridableIdGenerator().also { it.initialize(annotation, member, null) }

        val params = Properties()
        generator.configure(context, params)

        assertThat(params[SequenceStyleGenerator.SEQUENCE_PARAM]).isEqualTo("test_seq")
        assertThat(params[SequenceStyleGenerator.INCREMENT_PARAM]).isEqualTo(50)
    }

    @Test
    fun `configures from field annotation`() {
        val annotation = GeneratedId("field_gen")
        val field = EntityWithFieldAnnotation::class.java.getDeclaredField("id")
        val generator = OverridableIdGenerator().also { it.initialize(annotation, field, null) }

        val params = Properties()
        generator.configure(context, params)

        assertThat(params[SequenceStyleGenerator.SEQUENCE_PARAM]).isEqualTo("field_seq")
        assertThat(params[SequenceStyleGenerator.INITIAL_PARAM]).isEqualTo(100)
    }

    @Test
    fun `throws exception if generator not found`() {
        val annotation = GeneratedId("missing_gen")
        val field = EntityWithFieldAnnotation::class.java.getDeclaredField("id")
        val generator = OverridableIdGenerator().also { it.initialize(annotation, field, null) }

        val exception = assertThrows<IllegalArgumentException> {
            generator.configure(context, Properties())
        }
        assertThat(exception.message).isEqualTo("No sequence generator annotation found with name missing_gen")
    }

    @Test
    fun `returns assigned id if present and non-zero`() {
        val annotation = GeneratedId("test_gen")
        val member = mock<Member>()
        val generator = OverridableIdGenerator().also { it.initialize(annotation, member, null) }
        val entity = Any()

        whenever(session.getEntityPersister(anyOrNull(), anyOrNull())).thenReturn(persister)
        whenever(persister.getIdentifier(entity, session)).thenReturn(999L)

        val result = generator.generate(session, entity)

        assertThat(result).isEqualTo(999L)
    }

    @Test
    fun `triggers sequence generation if id is zero`() {
        val annotation = GeneratedId("test_gen")
        val member = EntityWithClassAnnotation::class.java.getDeclaredConstructor() as Member
        val generator = OverridableIdGenerator().also { it.initialize(annotation, member, null) }
        val entity = Any()

        whenever(session.getEntityPersister(anyOrNull(), anyOrNull())).thenReturn(persister)
        whenever(persister.getIdentifier(entity, session)).thenReturn(0L)

        assertThrows<Exception> {
            generator.generate(session, entity)
        }
    }

    @Test
    fun `triggers sequence generation if id is null`() {
        val annotation = GeneratedId("test_gen")
        val member = EntityWithClassAnnotation::class.java.getDeclaredConstructor() as Member
        val generator = OverridableIdGenerator().also { it.initialize(annotation, member, null) }
        val entity = Any()

        whenever(session.getEntityPersister(anyOrNull(), anyOrNull())).thenReturn(persister)
        whenever(persister.getIdentifier(entity, session)).thenReturn(null)

        assertThrows<Exception> {
            generator.generate(session, entity)
        }
    }

    @Test
    fun `allowAssignedIdentifiers is true`() {
        assertThat(OverridableIdGenerator().allowAssignedIdentifiers()).isTrue()
    }
}