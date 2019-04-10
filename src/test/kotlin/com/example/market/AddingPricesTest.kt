package com.example.market

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.ThreadLocalRandom

@RunWith(SpringRunner::class)
@SpringBootTest
class AddingPricesTest {

	@Autowired
	lateinit var productRepository : ProductRepository

	@Autowired
	lateinit var productPriceRepository : ProductPriceRepository

	@Autowired
	lateinit var productPricesManager : ProductPricesManager

	lateinit var product : Product
	var list = LinkedList<ProductPrice>()

	val random : ThreadLocalRandom = ThreadLocalRandom.current()
	val offset : ZoneOffset = ZoneId.systemDefault().rules.getOffset(Instant.now())

	@After
	fun tearDown() {
		productPriceRepository.deleteAll()
		productRepository.deleteAll()
	}

	@Before
	fun setUp() {
		product = Product("Random name")
		product = productRepository.save(product)

		var startDate = LocalDateTime.ofEpochSecond(0 , 0 , offset)
		do {
			val endDate = startDate.plusDays(random.nextLong((365 * 0.5).toLong() , (365 * 1.5).toLong()))
			val price = random.nextDouble(0.0 , 200.0)

			list.addLast(ProductPrice(price ,
			                          startDate ,
			                          endDate ,
			                          product
			                         ))

			productPricesManager.addPrice(product.id!! , startDate , endDate , price)

			startDate = endDate
		} while(endDate.isBefore(LocalDateTime.now()))
	}

	@Test
	fun allPeriods() {
		list.forEach { productPrice ->
			val randomDayInPeriod =
					randomDateWithBounds(productPrice.startDate , productPrice.endDate)
			val listProducts = productPricesManager.listProducts(randomDayInPeriod)
			assertEquals(1 , listProducts.entries.size)
			assertEquals(product.id!! , listProducts.keys.first().id!!)
			assertEquals(productPrice.price , listProducts.values.first())
		}
	}

	@Test
	fun overridePeriod() {
		val period = list[1]
//		check new period can override old one
		val newPrice = random.nextDouble(0.0 , 200.0)
		val oldStartDate = period.startDate
		val oldEndDate = period.endDate
		val newStartDate = oldStartDate.minusDays(1)
		val newEndDate = oldEndDate.plusDays(1)

		productPricesManager.addPrice(product.id!! , newStartDate , newEndDate , newPrice)

		val listProducts = productPricesManager.listProducts(randomDateWithBounds(newStartDate , newEndDate))
		assertEquals(1 , listProducts.entries.size)
		assertEquals(product.id!! , listProducts.keys.first().id!!)
		assertEquals(newPrice , listProducts.values.first())
	}

	@Test
	fun splitPeriod() {
		val period = list[list.size / 2]
//		check new period can split old one
		val oldPrice = period.price
		val newPrice = random.nextDouble(0.0 , 200.0)
		val oldStartDate = period.startDate
		val oldEndDate = period.endDate
		val newStartDate = oldStartDate.plusDays(10)
		val newEndDate = oldEndDate.minusDays(10)

		productPricesManager.addPrice(product.id!! , newStartDate , newEndDate , newPrice)

		var listProducts = productPricesManager.listProducts(randomDateWithBounds(oldStartDate , newStartDate))
		assertEquals(oldPrice , listProducts.values.first())
		listProducts = productPricesManager.listProducts(randomDateWithBounds(newStartDate , newEndDate))
		assertEquals(newPrice , listProducts.values.first())
		listProducts = productPricesManager.listProducts(randomDateWithBounds(newEndDate , oldEndDate))
		assertEquals(oldPrice , listProducts.values.first())
	}

	@Test
	fun stayBetweenPeriods() {
		val firstPeriod = list[list.size - 2]
		val secondPeriod = list[list.size - 1]
//		check new period can stay between old periods
		val firstOldPrice = firstPeriod.price
		val secondOldPrice = secondPeriod.price
		val newPrice = random.nextDouble(0.0 , 200.0)
		val oldStartDate = firstPeriod.startDate
		val oldEndDate = secondPeriod.endDate
		val newStartDate = randomDateWithBounds(firstPeriod.startDate.plusDays(10) , firstPeriod.endDate)
		val newEndDate = randomDateWithBounds(secondPeriod.startDate , secondPeriod.endDate.minusDays(10))

		productPricesManager.addPrice(product.id!! , newStartDate , newEndDate , newPrice)

		var listProducts = productPricesManager.listProducts(randomDateWithBounds(oldStartDate , newStartDate))
		assertEquals(firstOldPrice , listProducts.values.first())
		listProducts = productPricesManager.listProducts(randomDateWithBounds(newStartDate , newEndDate))
		assertEquals(newPrice , listProducts.values.first())
		listProducts = productPricesManager.listProducts(randomDateWithBounds(newEndDate , oldEndDate))
		assertEquals(secondOldPrice , listProducts.values.first())
	}

	private fun randomDateWithBounds(origin : LocalDateTime , bound : LocalDateTime) =
			LocalDateTime.ofEpochSecond(random.nextLong(origin.toEpochSecond(offset) ,
			                                            bound.toEpochSecond(offset)) ,
			                            0 ,
			                            offset)
}