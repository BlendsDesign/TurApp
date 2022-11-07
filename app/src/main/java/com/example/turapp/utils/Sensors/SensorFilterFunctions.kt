package com.example.turapp.utils.Sensors

import kotlin.math.abs

class SensorFilterFunctions() {

    //Filter Theory: Kalman

    var errorInEstimate = 0 // A: original error estimate feeds into it
    var errorInData = 0 // B: measurement
    var calkKalmanGain = 0 // Equation 1 : Puts relative importance on A or B

    var previousEstimate = 0 // C: original estimate feeds into it
    var measuredValue = 0 // D: data input
    var calcCurrentEstimate = 0 // Equation 2: updates estimate and puts relative importance on C or D

    var calkNewErrorInEstimate = 0 // Equation 3: feeds back into errorInEstimate

    var lastOutput = mutableListOf(0f, 0f, 0f)

    /*
     * time smoothing constant for low-pass filter 0 ≤ alpha ≤ 1 ; a smaller
     * value basically means more smoothing See: http://en.wikipedia.org/wiki
     * /Low-pass_filter#Discrete-time_realization
     */

    fun lowPass(input: List<Float>, alpha: Float): MutableList<Float> {


        for (i in input.indices) {
            if (abs(input[i] - lastOutput[i]) > 170) {
                lastOutput[i] = input[i]
                return lastOutput
            }
            lastOutput[i] = lastOutput[i] + alpha * (input[i] - lastOutput[i])
        }
        return lastOutput
    }

    fun limitValues(prevAcc: Float, currAcc: Float, limit: Float): Boolean {
        if (abs(currAcc - prevAcc) < limit)
            return true; // below limit, do not update values

        return false
    }

    fun gyroFilter(
        event: MutableList<Float>,
        previousEvent: MutableList<Float>
    ): MutableList<Float> {
        // high-pass filter. From lecture on filtering: Y = a*y1+ a*(x2 - x1)
        //(Gravity new) = alpha * (gravity old) + alpha * (event.values old - event.values new)
        val alpha = 0.8F

        previousEvent[0] = alpha * previousEvent[0] + alpha * (previousEvent[0] - event[0])
        previousEvent[1] = alpha * previousEvent[1] + alpha * (previousEvent[1] - event[0])
        previousEvent[2] = alpha * previousEvent[2] + alpha * (previousEvent[2] - event[0])

        return previousEvent
    }

    //https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-raw-data
    // getting rid of gravity from raw acc data
    fun filterOutGravity(
        event: MutableList<Float>,
        previousEvent: MutableList<Float>
    ): MutableList<Float> {

        val alpha = 0.8f
        val linearAcceleration = mutableListOf<Float>()

        // Isolate the force of previousEvent with the low-pass filter.
        previousEvent[0] = alpha * previousEvent[0] + (1 - alpha) * event[0]
        previousEvent[1] = alpha * previousEvent[1] + (1 - alpha) * event[1]
        previousEvent[2] = alpha * previousEvent[2] + (1 - alpha) * event[2]

        //Remove the gravity contribution with the high-pass filter.
        linearAcceleration.add(event[0] - previousEvent[0])
        linearAcceleration.add(event[1] - previousEvent[1])
        linearAcceleration.add(event[2] - previousEvent[2])

        return linearAcceleration
    }
}