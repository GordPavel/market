package com.example.market

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.*
import java.util.UUID.randomUUID
import java.util.stream.Collectors.toList
import java.util.stream.IntStream.rangeClosed

@RunWith(SpringRunner::class)
@SpringBootTest
class ProductsTest {

	@Autowired
	lateinit var productRepository : ProductRepository

	val products : List<Product> = rangeClosed(1 , 20)
			.mapToObj { "Product $it" }
			.map(::Product)
			.collect(toList())!!

	@Before
	fun setUp() {
		productRepository.saveAll(products)
	}

	@After
	fun tearDown() {
		productRepository.deleteAll()
	}

	@Test
	fun findAll() {
		val extracted = productRepository.findAll()

		assertTrue(extracted.map(Product::id).all(Objects::nonNull))

		assertEquals(products.map(Product::id) , extracted.map(Product::id))
		assertEquals(products.map(Product::name) , extracted.map(Product::name))
	}

	@Test
	fun findById() {
		val product = products.first()
		val extracted = productRepository.findById(product.id!!)
		assertTrue(extracted.map { it.id?.equals(product.id!!) ?: false }.orElse(false))
		assertTrue(extracted.map { it.name == product.name }.orElse(false))

		val optional = productRepository.findById(randomUUID())
		assertTrue(optional.isEmpty)
	}

	@Test
	fun change() {
		val product = products.last()
		product.name = "Change name"
		productRepository.save(product)

		val extracted = productRepository.findById(product.id!!)
		assertEquals(product.name , extracted.map { it.name }.orElse(null))
	}

	@Test
	fun delete() {
		val product = products[6]
		productRepository.delete(product)

		val optional = productRepository.findById(product.id!!)
		assertTrue(optional.isEmpty)
	}
}
