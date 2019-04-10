package com.example.market

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.TransactionSystemException
import java.time.LocalDateTime.now

@RunWith(SpringRunner::class)
@SpringBootTest
class SimplePriceTest {

	@Autowired
	lateinit var productRepository : ProductRepository

	@Autowired
	lateinit var productPriceRepository : ProductPriceRepository

	@Autowired
	lateinit var productPricesManager : ProductPricesManager

	lateinit var product : Product

	@Before
	fun setUp() {
		product = Product("Random name")
		product = productRepository.save(product)
	}

	@After
	fun tearDown() {
		productPriceRepository.deleteAll()
		productRepository.deleteAll()
	}


	@Test
	fun priceForOneProduct() {
		val productId = product.id!!
		var price = 50.0

		productPricesManager.addPrice(productId , now().minusDays(1) , now().plusDays(1) , price)

		var listProducts = productPricesManager.listProducts(now())

		assertEquals(1 , listProducts.entries.size)
		assertEquals(productId , listProducts.keys.first().id!!)
		assertEquals(price , listProducts.values.first())

		price = 100.0
		productPricesManager.addPrice(productId , now().plusDays(5) , now().plusDays(10) , price)

		listProducts = productPricesManager.listProducts(now().plusDays(7))

		assertEquals(1 , listProducts.entries.size)
		assertEquals(productId , listProducts.keys.first().id!!)
		assertEquals(price , listProducts.values.first())
	}

	@Test
	fun zeroProductsOnDate() {
		val listProducts = productPricesManager.listProducts(now().plusDays(4))
		assertEquals(0 , listProducts.entries.size)
	}

	@Test
	fun incorrectPrice() {
		val price = -1.0
		assertThrows(TransactionSystemException::class.java) {
			productPricesManager.addPrice(product.id!! , now().plusDays(5) , now().plusDays(10) , price)
		}
	}

	@Test
	fun incorrectDates() {
		assertThrows(TransactionSystemException::class.java) {
			productPricesManager.addPrice(product.id!! , now().plusDays(5) , now().minusDays(5) , 50.0)
		}
	}
}