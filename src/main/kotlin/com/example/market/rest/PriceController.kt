package com.example.market.rest

import com.example.market.ProductPricesManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.ZoneId.systemDefault
import java.time.ZonedDateTime
import java.util.*

@RestController
@RequestMapping("/prices")
class PriceController {

	@Autowired
	private lateinit var priceManager : ProductPricesManager

	@RequestMapping("/new" , method = [RequestMethod.PUT])
	@ResponseStatus(value = CREATED)
	fun newProduct(@RequestParam productId : UUID ,
	               @RequestParam(defaultValue = "01.01.1970 01:00:00 GMT")
	               @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss z")
	               startDate : ZonedDateTime ,
	               @RequestParam(defaultValue = "31.12.4000 23:59:59 GMT")
	               @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss z")
	               endDate : ZonedDateTime ,
	               @RequestParam price : Double) =
			priceManager.addPrice(productId ,
			                      startDate.withZoneSameInstant(systemDefault()).toLocalDateTime() ,
			                      endDate.withZoneSameInstant(systemDefault()).toLocalDateTime() ,
			                      price)

}