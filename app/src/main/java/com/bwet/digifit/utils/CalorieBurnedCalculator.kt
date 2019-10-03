package com.bwet.digifit.utils

object CalorieBurnedCalculator {

    private const val WALKING_FACTOR = 0.57

    // walking, 2.5 mph (5km/h), level, firm surface
    // source https://golf.procon.org/met-values-for-800-activities/
    private const val METS_FOR_WALKING = 3.0

    var weight = 67.0 //kg
    var height = 175.0 //cm

    private val strideLength = height * 0.415
    private val step_in_1_km = 100000 / strideLength
    private val calorieBurnedPerKm = WALKING_FACTOR * weight
    private val conversionFactor = calorieBurnedPerKm / step_in_1_km

//    val distanceTraveled = (steps * strideLength) / 100000 // km

    // source https://fitness.stackexchange.com/a/25500
    // returns calories burned in @param steps (step count) in cal
    fun calculateForSteps(steps: Int): Double = steps * conversionFactor

    fun calculateWalkingMETSForDuration(durationInHours: Int): Double = METS_FOR_WALKING * weight * durationInHours
}