package com.example.market

import ch.qos.logback.core.joran.spi.NoAutoStart
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy
import ch.qos.logback.core.rolling.RolloverFailure


@NoAutoStart
class StartupTimeBasedTriggeringPolicy<E> : DefaultTimeBasedFileNamingAndTriggeringPolicy<E>() {

	override fun start() {
		super.start()
		nextCheck = 0L
		isTriggeringEvent(null , null)
		try {
			tbrp.rollover()
		} catch(e : RolloverFailure) {
		}
	}
}