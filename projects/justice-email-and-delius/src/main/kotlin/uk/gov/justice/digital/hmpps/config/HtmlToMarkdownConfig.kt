package uk.gov.justice.digital.hmpps.config

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HtmlToMarkdownConfig {
    @Bean
    fun htmlToMarkdownConverter() = FlexmarkHtmlConverter.builder().build()
}