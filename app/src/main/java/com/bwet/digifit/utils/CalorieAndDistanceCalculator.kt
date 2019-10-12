package com.bwet.digifit.utils

object CalorieAndDistanceCalculator {

    private const val MET_WALKING_FACTOR = 3.5    // per min with slow pace, <3.5 Mph, walking the dog
    private const val MET_FAST_WALKING_FACTOR = 8.3 // per min with the speed of 5 Mph speed
    private const val MET_RUNNING_FACTOR = 6.0  //jogging, < 4 Mph
    private const val MET_FAST_RUNNING_FACTOR = 23.0 //// per min with the speed of 14 Mph speed


    //source https://www.freedieting.com/calories-burned
    // source https://golf.procon.org/met-values-for-800-activities/
    //source http://www.calories-calculator.net/Calculator_Formulars.html
    //Calories Burned (kcal) = METs x (WEIGHT_IN_KILOGRAM) x (DURATION_IN_HOUR)
    var weight = User.weight //kg
    var height = User.height //cm

    private val strideLength = height * 0.415
    private val step_in_1_km = 100000 / strideLength
    private val calorieBurnedPerKm = MET_WALKING_FACTOR * weight
    private val conversionFactor = calorieBurnedPerKm / step_in_1_km

//    val distanceTraveled = (steps * strideLength) / 100000 // km

    // source https://fitness.stackexchange.com/a/25500
    // returns calories burned in @param steps (step count) in cal

    fun calculateCalories(duration_in_hour: Int, activity:String, speed:Double):Double{
       var caloriesBurned = 0.0
        if(activity == "Walking"){
            if(speed >0.0 && speed <= 3.5){
                caloriesBurned = MET_WALKING_FACTOR * User.weight* duration_in_hour
            }else{
                caloriesBurned = MET_FAST_WALKING_FACTOR * User.weight * duration_in_hour
            }
        }

       if(activity == "Running"){

           if(speed >0.0 && speed <= 4.0){
               caloriesBurned = MET_RUNNING_FACTOR * User.weight * duration_in_hour
           }else{
               caloriesBurned = MET_FAST_RUNNING_FACTOR * User.weight * duration_in_hour
           }
       }
       return caloriesBurned
   }


    fun calculateForSteps(steps: Int): Double = steps * conversionFactor

    fun calculateWalkingMETSForDuration(durationInHours: Int): Double = MET_WALKING_FACTOR * weight * durationInHours

    fun calculateDistanceForSteps(steps: Int): Double = strideLength * steps * 0.8
}