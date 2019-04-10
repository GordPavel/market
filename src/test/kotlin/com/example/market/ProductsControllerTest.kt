package com.example.market

import org.junit.After
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.util.UriComponentsBuilder
import java.util.function.Predicate
import java.util.regex.Pattern

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductsControllerTest {

	@Autowired
	private lateinit var restTemplate : TestRestTemplate

	@Autowired
	lateinit var productRepository : ProductRepository

	val uuidPredicate : Predicate<String> =
			Pattern.compile(
					"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
			               ).asPredicate()

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
					contentType = TEXT_PLAIN
				}) ,
				String::class.java)
		assertEquals(CREATED , response.statusCode)
		assertTrue(uuidPredicate.test(response.body!!))
	}

	@Test
	fun delete() {
		val savedUUID = restTemplate.exchange(
				UriComponentsBuilder.fromPath("/products/new")
						.queryParam("name" , "Random name")
						.build()
						.toUriString() ,
				PUT ,
				HttpEntity(null , HttpHeaders().apply {
					contentType = TEXT_PLAIN
				}) ,
				String::class.java).body!!
		val deleteResponse = restTemplate.exchange(
				UriComponentsBuilder.fromPath("/products/delete")
						.queryParam("id" , savedUUID)
						.build()
						.toUriString() ,
				DELETE ,
				HttpEntity(null , HttpHeaders().apply {
					contentType = TEXT_PLAIN
				}) ,
				Void::class.java)
		assertEquals(ACCEPTED , deleteResponse.statusCode)
	}
}