package com.example.market

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation.REQUIRES_NEW
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.stream.Collectors.collectingAndThen
import java.util.stream.Collectors.toMap
import javax.persistence.criteria.Root

interface ProductPricesManager {
	fun listProducts(date : LocalDate) : Map<Product , BigDecimal>

	fun addPrice(productId : UUID ,
	             startDate : LocalDate ,
	             endDate : LocalDate ,
	             price : BigDecimal)
}

@Service
@Transactional(propagation = REQUIRES_NEW)
class ProductPricesManagerImpl : ProductPricesManager {

	@Autowired
	@Qualifier("threadPool")
	lateinit var threadPool : ExecutorService

	@Autowired
	private lateinit var productRepository : ProductRepository

	@Autowired
	private lateinit var productPriceRepository : ProductPriceRepository

	@Autowired
	private lateinit var productPricesManager : ProductPricesManagerImpl

	@Transactional(readOnly = true)
	override fun listProducts(date : LocalDate) : Map<Product , BigDecimal> {
		val firstCollector = toMap<Product , Product , BigDecimal>({ it })
		{ pair -> pair.getPrice(date)!! }
		val clearingCollector = toMap<MutableMap.MutableEntry<Product , BigDecimal> , Product , BigDecimal>({ it.key })
		{ it.value.setScale(2 , RoundingMode.HALF_EVEN) }

		return productRepository.listWithPrices(date)
				.collect(collectingAndThen(firstCollector) { map ->
					map.entries.stream()
							.peek { entry -> entry.key.prices = emptyArray() }
							.collect(clearingCollector)
				})
	}

	@Transactional
	override fun addPrice(productId : UUID , startDate : LocalDate , endDate : LocalDate , price : BigDecimal) {
		val product =
				productRepository.findByIdOrNull(productId)
				?: throw IllegalArgumentException("No product with $productId id")

		val latch = CountDownLatch(4)

		threadPool.execute {
			val innerOverlap = productPricesManager.listInnerOverlapPricePeriods(productId ,
			                                                                     startDate ,
			                                                                     endDate)
			productPriceRepository.deleteAll(innerOverlap)
			latch.countDown()
		}

		threadPool.execute {
			val leftOverlap = productPricesManager.listLeftOverlapPricePeriods(productId ,
			                                                                   startDate ,
			                                                                   endDate)
			leftOverlap.toMutableList().stream()
					.peek { it.endDate = startDate }.forEach { productPriceRepository.save(it) }
			latch.countDown()
		}

		threadPool.execute {
			val rightOverlap = productPricesManager.listRightOverlapPricePeriods(productId ,
			                                                                     startDate ,
			                                                                     endDate)
			rightOverlap.toMutableList().stream()
					.peek { it.startDate = endDate }.forEach { productPriceRepository.save(it) }
			latch.countDown()
		}

		threadPool.execute {
			val outerOverlap = productPricesManager.listOuterOverlapPricePeriods(productId ,
			                                                                     startDate ,
			                                                                     endDate)
			outerOverlap.toList().stream()
					.peek {
						productPriceRepository.save(ProductPrice(it.price ,
						                                         endDate ,
						                                         it.endDate ,
						                                         product))
					}
					.peek { it.endDate = startDate }
					.forEach { productPriceRepository.save(it) }
			latch.countDown()
		}



		latch.await()
		val newPrice = ProductPrice(price , startDate , endDate , product)
		productPriceRepository.save(newPrice)
	}

	@Transactional(readOnly = true)
	fun listInnerOverlapPricePeriods(productID : UUID ,
	                                 startDate : LocalDate ,
	                                 endDate : LocalDate) : Iterable<ProductPrice> =
			productPriceRepository.findAll { prodPrice , query , builder ->
				builder.and(builder.greaterThan(getStartDate(prodPrice) , startDate) ,
				            builder.lessThan(getEndDate(prodPrice) , endDate) ,
				            builder.equal(getProductId(prodPrice) , productID))
			}

	@Transactional(readOnly = true)
	fun listLeftOverlapPricePeriods(productID : UUID ,
	                                startDate : LocalDate ,
	                                endDate : LocalDate) : Iterable<ProductPrice> =
			productPriceRepository.findAll { prodPrice , query , builder ->
				builder.and(builder.lessThan(getStartDate(prodPrice) , startDate) ,
				            builder.greaterThan(getEndDate(prodPrice) , startDate) ,
				            builder.lessThan(getEndDate(prodPrice) , endDate) ,
				            builder.equal(getProductId(prodPrice) , productID))
			}

	@Transactional(readOnly = true)
	fun listRightOverlapPricePeriods(productID : UUID ,
	                                 startDate : LocalDate ,
	                                 endDate : LocalDate) : Iterable<ProductPrice> =
			productPriceRepository.findAll { prodPrice , query , builder ->
				builder.and(builder.greaterThan(getStartDate(prodPrice) , startDate) ,
				            builder.lessThan(getStartDate(prodPrice) , endDate) ,
				            builder.greaterThan(getEndDate(prodPrice) , endDate) ,
				            builder.equal(getProductId(prodPrice) , productID))
			}

	@Transactional(readOnly = true)
	fun listOuterOverlapPricePeriods(productID : UUID ,
	                                 startDate : LocalDate ,
	                                 endDate : LocalDate) : Iterable<ProductPrice> =
			productPriceRepository.findAll { prodPrice , query , builder ->
				builder.and(builder.lessThan(getStartDate(prodPrice) , startDate) ,
				            builder.greaterThan(getEndDate(prodPrice) , endDate) ,
				            builder.equal(getProductId(prodPrice) , productID))
			}

	private fun getStartDate(prodPrice : Root<ProductPrice>) = prodPrice.get<LocalDate>("startDate")
	private fun getEndDate(prodPrice : Root<ProductPrice>) = prodPrice.get<LocalDate>("endDate")
	private fun getProductId(prodPrice : Root<ProductPrice>) = prodPrice.get<Product>("product").get<UUID>("id")

}
