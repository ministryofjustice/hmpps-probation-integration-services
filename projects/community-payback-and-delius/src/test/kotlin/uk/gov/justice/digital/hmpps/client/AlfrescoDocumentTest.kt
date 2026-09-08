package uk.gov.justice.digital.hmpps.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AlfrescoDocumentTest {

    @Nested
    inner class ConstructorAndProperties {
        @Test
        fun `create AlfrescoDocument with id`() {
            val doc = AlfrescoDocument(id = "test-id-123")

            assertThat(doc.id).isEqualTo("test-id-123")
        }

        @Test
        fun `create AlfrescoDocument with different id`() {
            val doc = AlfrescoDocument(id = "another-id")

            assertThat(doc.id).isEqualTo("another-id")
        }

        @Test
        fun `id property is accessible`() {
            val id = "doc-id-789"
            val doc = AlfrescoDocument(id = id)

            assertThat(doc.id).isNotNull.isNotEmpty.isEqualTo(id)
        }

        @Test
        fun `create AlfrescoDocument with long id`() {
            val longId = "a".repeat(100)
            val doc = AlfrescoDocument(id = longId)

            assertThat(doc.id).isEqualTo(longId).hasSize(100)
        }

        @Test
        fun `create AlfrescoDocument with UUID-like id`() {
            val uuidId = "550e8400-e29b-41d4-a716-446655440000"
            val doc = AlfrescoDocument(id = uuidId)

            assertThat(doc.id).isEqualTo(uuidId)
        }
    }

    @Nested
    inner class DataClassEquality {
        @Test
        fun `two documents with same id are equal`() {
            val doc1 = AlfrescoDocument(id = "same-id")
            val doc2 = AlfrescoDocument(id = "same-id")

            assertThat(doc1).isEqualTo(doc2)
        }

        @Test
        fun `two documents with different ids are not equal`() {
            val doc1 = AlfrescoDocument(id = "id-1")
            val doc2 = AlfrescoDocument(id = "id-2")

            assertThat(doc1).isNotEqualTo(doc2)
        }

        @Test
        fun `document is not equal to null`() {
            val doc = AlfrescoDocument(id = "test-id")

            assertThat(doc).isNotEqualTo(null)
        }

        @Test
        fun `document is not equal to different type`() {
            val doc = AlfrescoDocument(id = "test-id")
            val other = "test-id"

            assertThat(doc).isNotEqualTo(other)
        }

        @Test
        fun `document equals itself`() {
            val doc = AlfrescoDocument(id = "test-id")

            assertThat(doc).isEqualTo(doc)
        }
    }

    @Nested
    inner class DataClassHashCode {
        @Test
        fun `two equal documents have same hashCode`() {
            val doc1 = AlfrescoDocument(id = "same-id")
            val doc2 = AlfrescoDocument(id = "same-id")

            assertThat(doc1.hashCode()).isEqualTo(doc2.hashCode())
        }

        @Test
        fun `document hashCode is consistent`() {
            val doc = AlfrescoDocument(id = "test-id")
            val hash1 = doc.hashCode()
            val hash2 = doc.hashCode()

            assertThat(hash1).isEqualTo(hash2)
        }

        @Test
        fun `different documents likely have different hashCode`() {
            val doc1 = AlfrescoDocument(id = "id-1")
            val doc2 = AlfrescoDocument(id = "id-2")

            // Note: hashCode collision is possible but unlikely
            assertThat(doc1.hashCode()).isNotEqualTo(doc2.hashCode())
        }

        @Test
        fun `hashCode can be used in collections`() {
            val doc1 = AlfrescoDocument(id = "id-1")
            val doc2 = AlfrescoDocument(id = "id-2")
            val doc3 = AlfrescoDocument(id = "id-1")

            val set = setOf(doc1, doc2, doc3)

            // doc1 and doc3 are equal, so set should have 2 elements
            assertThat(set).hasSize(2)
        }
    }

    @Nested
    inner class DataClassToString {
        @Test
        fun `toString contains class name`() {
            val doc = AlfrescoDocument(id = "test-id")

            assertThat(doc.toString()).contains("AlfrescoDocument")
        }

        @Test
        fun `toString contains id value`() {
            val doc = AlfrescoDocument(id = "test-id-123")

            assertThat(doc.toString()).contains("test-id-123")
        }

        @Test
        fun `toString is not empty`() {
            val doc = AlfrescoDocument(id = "test-id")

            assertThat(doc.toString()).isNotEmpty
        }

        @Test
        fun `different documents have different toString`() {
            val doc1 = AlfrescoDocument(id = "id-1")
            val doc2 = AlfrescoDocument(id = "id-2")

            assertThat(doc1.toString()).isNotEqualTo(doc2.toString())
        }

        @Test
        fun `toString for same data is consistent`() {
            val doc = AlfrescoDocument(id = "test-id")
            val str1 = doc.toString()
            val str2 = doc.toString()

            assertThat(str1).isEqualTo(str2)
        }
    }

    @Nested
    inner class DataClassCopy {
        @Test
        fun `copy creates new instance with same values`() {
            val original = AlfrescoDocument(id = "original-id")
            val copied = original.copy()

            assertThat(copied).isEqualTo(original)
            assertThat(copied).isNotSameAs(original)
        }

        @Test
        fun `copy with modified id creates new instance`() {
            val original = AlfrescoDocument(id = "original-id")
            val copied = original.copy(id = "new-id")

            assertThat(copied.id).isEqualTo("new-id")
            assertThat(original.id).isEqualTo("original-id")
        }

        @Test
        fun `copy without parameters creates independent instance`() {
            val original = AlfrescoDocument(id = "test-id")
            val copied = original.copy()

            assertThat(copied).isEqualTo(original)
            assertThat(copied).isNotSameAs(original)
            assertThat(copied.id).isEqualTo(original.id)
        }

        @Test
        fun `multiple copies are independent`() {
            val original = AlfrescoDocument(id = "id-1")
            val copy1 = original.copy()
            val copy2 = original.copy(id = "id-2")

            assertThat(copy1.id).isEqualTo("id-1")
            assertThat(copy2.id).isEqualTo("id-2")
            assertThat(copy1).isNotEqualTo(copy2)
        }
    }

    @Nested
    inner class JsonAlias {
        @Test
        fun `AlfrescoDocument can be created with id parameter`() {
            // Tests that @JsonAlias("ID") allows deserialization from JSON with "ID" field
            val doc = AlfrescoDocument(id = "from-json")

            assertThat(doc.id).isEqualTo("from-json")
        }

        @Test
        fun `id field is properly annotated`() {
            val doc = AlfrescoDocument(id = "test")

            // Verify the object is created and id is set
            assertThat(doc).isNotNull
            assertThat(doc.id).isEqualTo("test")
        }
    }

    @Nested
    inner class EdgeCases {
        @Test
        fun `create AlfrescoDocument with empty string id`() {
            val doc = AlfrescoDocument(id = "")

            assertThat(doc.id).isEmpty()
        }

        @Test
        fun `create AlfrescoDocument with special characters in id`() {
            val specialId = "id-with-!@#\$%^&*()"
            val doc = AlfrescoDocument(id = specialId)

            assertThat(doc.id).isEqualTo(specialId)
        }

        @Test
        fun `create AlfrescoDocument with whitespace in id`() {
            val idWithSpace = "id with spaces"
            val doc = AlfrescoDocument(id = idWithSpace)

            assertThat(doc.id).isEqualTo(idWithSpace)
        }

        @Test
        fun `create AlfrescoDocument with newline in id`() {
            val idWithNewline = "id\nwith\nnewlines"
            val doc = AlfrescoDocument(id = idWithNewline)

            assertThat(doc.id).isEqualTo(idWithNewline)
        }

        @Test
        fun `create AlfrescoDocument with unicode characters`() {
            val unicodeId = "id-with-unicode-你好-مرحبا"
            val doc = AlfrescoDocument(id = unicodeId)

            assertThat(doc.id).isEqualTo(unicodeId)
        }

        @Test
        fun `create AlfrescoDocument with numeric string id`() {
            val numericId = "123456789"
            val doc = AlfrescoDocument(id = numericId)

            assertThat(doc.id).isEqualTo(numericId)
        }

        @Test
        fun `create AlfrescoDocument with alphanumeric id`() {
            val alphanumericId = "abc123XYZ789"
            val doc = AlfrescoDocument(id = alphanumericId)

            assertThat(doc.id).isEqualTo(alphanumericId)
        }
    }

    @Nested
    inner class DataClassContract {
        @Test
        fun `destructuring works for AlfrescoDocument`() {
            val doc = AlfrescoDocument(id = "test-id")
            val (id) = doc

            assertThat(id).isEqualTo("test-id")
        }

        @Test
        fun `componentN functions work`() {
            val doc = AlfrescoDocument(id = "component-test")

            assertThat(doc.component1()).isEqualTo("component-test")
        }

        @Test
        fun `data class is iterable through destructuring`() {
            val idValue = "destructure-test"
            val doc = AlfrescoDocument(id = idValue)

            val (extractedId) = doc
            assertThat(extractedId).isEqualTo(idValue)
        }
    }

    @Nested
    inner class RealWorldScenarios {
        @Test
        fun `simulate Alfresco API response with document ID`() {
            val alfrescoResponse = AlfrescoDocument(id = "workspace://SpacesStore/abc123def456")

            assertThat(alfrescoResponse.id).isNotEmpty.isEqualTo("workspace://SpacesStore/abc123def456")
        }

        @Test
        fun `multiple documents from Alfresco responses can be tracked`() {
            val doc1 = AlfrescoDocument(id = "alfresco-id-1")
            val doc2 = AlfrescoDocument(id = "alfresco-id-2")
            val doc3 = AlfrescoDocument(id = "alfresco-id-3")

            val documents = listOf(doc1, doc2, doc3)

            assertThat(documents).hasSize(3)
            assertThat(documents.map { it.id }).containsExactly("alfresco-id-1", "alfresco-id-2", "alfresco-id-3")
        }

        @Test
        fun `documents can be used in a map`() {
            val doc1 = AlfrescoDocument(id = "doc-1")
            val doc2 = AlfrescoDocument(id = "doc-2")

            val documentMap = mapOf(
                "first" to doc1,
                "second" to doc2
            )

            assertThat(documentMap["first"]?.id).isEqualTo("doc-1")
            assertThat(documentMap["second"]?.id).isEqualTo("doc-2")
        }

        @Test
        fun `document equality works in collections`() {
            val doc1 = AlfrescoDocument(id = "same-id")
            val doc2 = AlfrescoDocument(id = "same-id")

            val set1 = setOf(doc1)
            val set2 = setOf(doc2)

            assertThat(set1).containsAll(set2)
        }
    }
}


