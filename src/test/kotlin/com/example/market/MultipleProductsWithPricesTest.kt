package com.example.market

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.math.BigDecimal
import java.time.LocalDate.now
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Collectors
import java.util.stream.IntStream.rangeClosed

@RunWith(SpringRunner::class)
@SpringBootTest
class MultipleProductsWithPricesTest {

	@Autowired
	lateinit var productRepository : ProductRepository

	@Autowired
	lateinit var productPriceRepository : ProductPriceRepository

	@Autowired
	lateinit var productPricesManager : ProductPricesManager

	val random : ThreadLocalRandom = ThreadLocalRandom.current()
	var products : List<Product> = rangeClosed(1 , 100)
			.mapToObj { Product("Product $it") }
			.collect(Collectors.toList())
	lateinit var chunked : List<List<Product>>

	@Before
	fun setUp() {
		products = productRepository.saveAll(products).toMutableList()
		chunked = products.chunked((products.size / 3.0 + 1).toInt())

		val threadPool = Executors.newFixedThreadPool(5)
		val latch = CountDownLatch(products.size)

//		first chunk with dates -15 – -5 for current moment,
//		second chunk with dates -5 – +5 for current moment,
//		first chunk with dates +5 – +15 for current moment,

		chunked
				.forEachIndexed { index , list ->
					val newIndex = (index - 1) * 2
					val startDate = now().plusDays((newIndex - 1) * 5L)
					val endDate = now().plusDays((newIndex + 1) * 5L)

					list.forEach { product ->
						threadPool.execute {
							productPricesManager.addPrice(product.id!! ,
							                              startDate ,
							                              endDate ,
							                              BigDecimal.valueOf(random.nextDouble(0.0 , 200.0)))
							latch.countDown()
						}
					}
				}
		latch.await()
	}

	@After
	fun tearDown() {
		productPriceRepository.deleteAll()
		productRepository.deleteAll()
	}

	@Test
	fun showOnlyProductsWithPriceOnDate() {
		var listProducts = productPricesManager.listProducts(now().minusDays(10))
		assertEquals(chunked[0].size , listProducts.size)
		listProducts = productPricesManager.listProducts(now())
		assertEquals(chunked[1].size , listProducts.size)
		listProducts = productPricesManager.listProducts(now().plusDays(10))
		assertEquals(chunked[2].size , listProducts.size)
	}
}