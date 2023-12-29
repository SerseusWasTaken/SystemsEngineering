package utils

import java.math.RoundingMode
import kotlin.math.nextDown

object Utils {
    fun getRandomNumber(min: Int, max:Int, numberOfDecimals: Int): Double {
        //Get random number in range [min, max] as per documentation of Math.Random()
        val f = Math.random()/ 1.0.nextDown()
        return (min*(1.0 - f) + max * f).toBigDecimal().setScale(numberOfDecimals, RoundingMode.HALF_UP).toDouble()
    }

    fun beTrueForChanceOf(likelihoodOfBeingTrue: Double): Boolean {
        return getRandomNumber(0, 100, 1) <= likelihoodOfBeingTrue
    }

    fun Double.round(decimals: Int): Double {
        return if(!this.isNaN())
            this.toBigDecimal().setScale(decimals, RoundingMode.HALF_UP).toDouble()
        else
            this
    }
}