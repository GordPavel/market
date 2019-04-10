package com.example.market.rest

import com.example.market.Product
import com.example.market.ProductPricesManager
import com.example.market.ProductRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.DELETE
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RequestMethod.PUT
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime.now
import java.time.ZoneId.systemDefault
import java.time.ZonedDateTime
import java.util.*

@RestController
@RequestMapping("/products")
class ProductController {

	@Autowired
	private lateinit var productRepository : ProductRepository

	@Autowired
	private lateinit var priceManager : ProductPricesManager

	@Autowired
	private lateinit var objectMapper : ObjectMapper

	@RequestMapping("/new" , method = [PUT] , produces = [TEXT_PLAIN_VALUE])
	@ResponseStatus(value = CREATED)
	fun newProduct(@RequestParam name : String) : String = productRepository.save(Product(name)).id!!.toString()

	@RequestMapping("/delete" , method = [DELETE])
	@ResponseStatus(value = ACCEPTED)
	fun deleteProduct(@RequestParam id : UUID) = productRepository.deleteById(id)

	@RequestMapping("/list" , method = [GET] , produces = [APPLICATION_JSON_UTF8_VALUE])
	fun listProducts(
			@RequestParam(required = false)
			@DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss z")
			date : ZonedDateTime?
	                ) =
			priceManager.listProducts(date?.withZoneSameInstant(systemDefault())?.toLocalDateTime() ?: now())
					.mapKeys { objectMapper.writeValueAsString(it.key) }
}