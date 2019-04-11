package com.example.market

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.hibernate.annotations.GenericGenerator
import org.springframework.lang.NonNull
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType.LAZY
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OrderColumn
import javax.persistence.Table
import javax.persistence.Transient
import javax.persistence.Version
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@Suppress("ArrayInDataClass")

@Entity
@Table(name = "products")
@JsonInclude(NON_EMPTY)
@JsonPropertyOrder(value = ["id" , "name"])
data class Product(
		@Id
		@GeneratedValue(generator = "uuid2")
		@GenericGenerator(name = "uuid2" , strategy = "org.hibernate.id.UUIDGenerator")
		@Column(name = "id" , columnDefinition = "CHAR(36)")
		var id : UUID? ,

		@NonNull
		@Basic
		@Column(name = "name")
		var name : String ,

		@Version
		@Column(name = "version")
		var version : Long ,

		@OneToMany(fetch = LAZY , mappedBy = "product")
		@OrderColumn(name = "order_column")
		var prices : Array<ProductPrice> = emptyArray()) {

	constructor(name : String) : this(null , name , 1 , emptyArray())

	@Transient
	fun getPrice(date : LocalDateTime) = prices
			.firstOrNull { isDateInsideRange(date , it.startDate , it.endDate) }?.price

	private fun isDateInsideRange(date : LocalDateTime , startRange : LocalDateTime , endRange : LocalDateTime) =
			date.isAfter(startRange) and date.isBefore(endRange)

	override fun equals(other : Any?) : Boolean {
		val product = other as Product?
		return this.id?.equals(product?.id) ?: (product?.id == null) and
		       (this.name == product?.name) ?: (product?.name == null)
	}

	override fun hashCode() = this.id?.hashCode() ?: this.name.hashCode()
}

@Entity
@Table(name = "prices")
@JsonInclude(NON_EMPTY)
@JsonPropertyOrder(value = ["id" , "price" , "startDate" , "endDate"])
@DatesArrangeConstraint
data class ProductPrice(

		@Id
		@GeneratedValue(generator = "uuid2")
		@GenericGenerator(name = "uuid2" , strategy = "org.hibernate.id.UUIDGenerator")
		@Column(name = "id" , columnDefinition = "CHAR(36)")
		var id : UUID? ,

		@NonNull
		@Column(name = "start_date_time")
		var startDate : LocalDateTime ,

		@NonNull
		@Column(name = "end_date_time")
		var endDate : LocalDateTime ,

		@Basic
		@NonNull
		@Column(name = "price")
		@DecimalMin(minValue = 0.0 , message = "Price can't be negative")
		var price : Double ,

		@Version
		@Column(name = "version")
		var version : Long ,

//		only for @OrderColumn in Product class
		@Basic
		@Column(name = "order_column")
		var order : Int = 0 ,

		@NonNull
		@ManyToOne(fetch = LAZY)
		@JoinColumn(name = "product_id" , updatable = false)
		var product : Product) {

	constructor(price : Double ,
	            startDate : LocalDateTime ,
	            endDate : LocalDateTime ,
	            product : Product) :
			this(null , startDate , endDate , price , 1 , 0 , product)

	override fun equals(other : Any?) : Boolean {
		val productPrice = other as ProductPrice?
		return this.id?.equals(productPrice?.id) ?: (productPrice?.id == null) and
		       (productPrice?.price?.equals(this.price) ?: false) and
		       (productPrice?.startDate?.equals(this.startDate) ?: false) and
		       (productPrice?.endDate?.equals(this.endDate) ?: false) and
		       (productPrice?.product?.equals(this.product) ?: false)
	}

	override fun hashCode() = this.id?.hashCode() ?: (
			31 * this.price.hashCode() +
			98 * this.startDate.hashCode() +
			3 * this.endDate.hashCode() +
			100 * this.product.hashCode()
	                                                 )
}

@Constraint(validatedBy = [DatesArrangeConstraintValidator::class])
@Target(CLASS)
@Retention(RUNTIME)
annotation class DatesArrangeConstraint(val message : String = "Start date should be before end date" ,
                                        val groups : Array<KClass<Any>> = [] ,
                                        val payload : Array<KClass<out Payload>> = [])

class DatesArrangeConstraintValidator : ConstraintValidator<DatesArrangeConstraint , ProductPrice> {
	override fun isValid(value : ProductPrice , context : ConstraintValidatorContext) =
			value.endDate.isAfter(value.startDate)
}

@Constraint(validatedBy = [DecimalMinValidator::class])
@Target(FIELD)
@Retention(RUNTIME)
annotation class DecimalMin(val message : String = "Value can't be negative" ,
                            val groups : Array<KClass<Any>> = [] ,
                            val payload : Array<KClass<out Payload>> = [] ,
                            val minValue : Double)

class DecimalMinValidator(private val minValue : Double = 0.0) : ConstraintValidator<DecimalMin , Double> {
	override fun isValid(value : Double , context : ConstraintValidatorContext) = value >= minValue
}
