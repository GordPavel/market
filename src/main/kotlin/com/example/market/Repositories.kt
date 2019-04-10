package com.example.market

import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation.REQUIRED
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Stream

@Repository
@Transactional(propagation = REQUIRED)
interface ProductRepository : PagingAndSortingRepository<Product , UUID> {

	@Query("from Product prod inner join fetch prod.prices price where :date between price.startDate and price.endDate")
	fun listWithPrices(date : LocalDateTime) : Stream<Product>
}

@Repository
@Transactional(propagation = REQUIRED)
interface ProductPriceRepository : PagingAndSortingRepository<ProductPrice , UUID> ,
                                   JpaSpecificationExecutor<ProductPrice>