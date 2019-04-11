package com.example.market

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofPattern

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PricesControllerTest {

	@Autowired
	private lateinit var restTemplate : TestRestTemplate

	@Autowired
	lateinit var productRepository : ProductRepository

	@Autowired
	lateinit var pricesRepository : ProductPriceRepository

	@Autowired
	lateinit var objectMapper : ObjectMapper

	var product = Product("Random name")
	val timePattern : DateTimeFormatter = ofPattern("dd.MM.yyyy")

	@Before
	fun setUp() {
		product = productRepository.save(product)
	}

	@After
	fun tearDown() {
		pricesRepository.deleteAll()
		productRepository.deleteAll()
	}

	@Test
	fun simpleAddPrice() {
		val price = 50.0
		val response = restTemplate.exchange(
				UriComponentsBuilder.fromPath("/prices/new")
						.queryParam("productId" , product.id!!)
						.queryParam("startDate" , now().minusDays(1).format(timePattern))
						.queryParam("endDate" , now().plusDays(1).format(timePattern))
						.queryParam("price" , price)
						.build()
						.toUriString() ,
				PUT ,
				HttpEntity(null , HttpHeaders().apply {
					contentType = MediaType.TEXT_PLAIN
				}) ,
				Void::class.java)
		assertEquals(HttpStatus.CREATED , response.statusCode)

		val listResponse = restTemplate.exchange(
				UriComponentsBuilder.fromPath("/products/list")
						.queryParam("date" , now().format(timePattern))
						.build()
						.toUriString() ,
				GET ,
				HttpEntity(null , HttpHeaders().apply {
					accept = listOf(APPLICATION_JSON_UTF8)
				}) ,
				Map::class.java
		                                        )
		assertEquals(price , listResponse.body?.values?.first() as Double)
		val extractedProduct = objectMapper.readValue(listResponse.body?.keys?.first() as String , Product::class.java)
		assertEquals(product.id!! , extractedProduct.id)
	}

	@Test
	fun openInterval() {
		val response = restTemplate.exchange(
				UriComponentsBuilder.fromPath("/prices/new")
						.queryParam("productId" , product.id!!)
						.queryParam("startDate" , now().format(timePattern))
						.queryParam("price" , 50.0)
						.build()
						.toUriString() ,
				PUT ,
				HttpEntity(null , HttpHeaders().apply {
					contentType = MediaType.TEXT_PLAIN
				}) ,
				Void::class.java)
		assertEquals(HttpStatus.CREATED , response.statusCode)
	}
}