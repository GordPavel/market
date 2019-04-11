package com.example.market

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.After
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.util.UriComponentsBuilder

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductsControllerTest {

	@Autowired
	private lateinit var restTemplate : TestRestTemplate

	@Autowired
	lateinit var productRepository : ProductRepository

	@Autowired
	private lateinit var objectMapper : ObjectMapper

	@After
	fun tearDown() = productRepository.deleteAll()

	@Test
	fun newProduct() {
		val response = restTemplate.exchange(
				UriComponentsBuilder.fromPath("/products/new")
						.queryParam("name" , "Random name")
						.build()
						.toUriString() ,
				PUT ,
				HttpEntity(null , HttpHeaders().apply {
					contentType = APPLICATION_JSON_UTF8
				}) ,
				String::class.java)
		assertEquals(CREATED , response.statusCode)
	}

	@Test
	fun delete() {
		val product = objectMapper.readValue(restTemplate.exchange(
				UriComponentsBuilder.fromPath("/products/new")
						.queryParam("name" , "Random name")
						.build()
						.toUriString() ,
				PUT ,
				HttpEntity(null , HttpHeaders().apply {
					contentType = APPLICATION_JSON_UTF8
				}) ,
				String::class.java).body!! , Product::class.java)
		val savedUUID = product.id!!

		val deleteResponse = restTemplate.exchange(
				UriComponentsBuilder.fromPath("/products/delete")
						.queryParam("id" , savedUUID)
						.build()
						.toUriString() ,
				DELETE ,
				HttpEntity(null , HttpHeaders()) ,
				Void::class.java)
		assertEquals(ACCEPTED , deleteResponse.statusCode)
	}
}