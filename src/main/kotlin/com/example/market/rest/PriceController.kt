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
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/prices")
class PriceController {

	@Autowired
	private lateinit var priceManager : ProductPricesManager

	@RequestMapping("/new" , method = [RequestMethod.PUT])
	@ResponseStatus(value = CREATED)
	fun newProduct(@RequestParam productId : UUID ,
	               @RequestParam(defaultValue = "01.01.1970")
	               @DateTimeFormat(pattern = "dd.MM.yyyy")
	               startDate : LocalDate ,
	               @RequestParam(defaultValue = "31.12.4000")
	               @DateTimeFormat(pattern = "dd.MM.yyyy")
	               endDate : LocalDate ,
	               @RequestParam price : BigDecimal) =
			priceManager.addPrice(productId , startDate , endDate , price)

}