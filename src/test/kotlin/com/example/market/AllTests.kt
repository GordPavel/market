package com.example.market

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses
import org.springframework.boot.test.context.SpringBootTest

@RunWith(Suite::class)
@SpringBootTest
@SuiteClasses(AddingPricesTest::class ,
              SimplePriceTest::class ,
              ProductsTest::class ,
              ProductsControllerTest::class ,
              PricesControllerTest::class ,
              MultipleProductsWithPricesTest::class)
class AllTests