package com.example.market

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.convert.converter.Converter
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters
import org.springframework.format.FormatterRegistry
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import java.util.UUID.fromString
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors.newWorkStealingPool


@EntityScan(basePackageClasses = [MarketApplication::class , Jsr310JpaConverters::class])
@SpringBootApplication
@EnableTransactionManagement
class MarketApplication : WebMvcConfigurerAdapter() {

	@Bean("threadPool")
	fun threadPool() : ExecutorService = newWorkStealingPool()

	@Bean
	fun objectMapper() = ObjectMapper()

	override fun addFormatters(registry : FormatterRegistry) {
		registry.addConverter(Converter { str : String -> fromString(str) })
	}
}

fun main(args : Array<String>) {
	runApplication<MarketApplication>(*args)
}
