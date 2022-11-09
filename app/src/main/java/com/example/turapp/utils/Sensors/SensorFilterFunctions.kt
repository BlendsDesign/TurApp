package com.example.turapp.utils.Sensors

import android.content.ContentValues
import kotlin.math.abs

class SensorFilterFunctions() {

    //Filter Theory: Kalman
    var errorInEstimate = 0
    // A: first time through : original error estimate, then next previous error in estimate feeds back into it

    var errorInDataMeasurement = 0
    // B: Data measurement error

    var kalmanGain = 0
    // Equation 1 : Puts relative importance on A or B, and determines weight distribution on C and D
    // If error in estimate is smaller then more importance is put into it, and vica versa.

    var previousEstimate = 0
    // C: first time through : original estimate feeds into it (original error can be anything)

    var measuredValue = 0
    // D: data input

    var currentEstimate = 0
    // Equation 2: updates estimate and puts relative importance on C or D

    var newErrorInEstimate = 0
    // Equation 3: feeds back into errorInEstimate

    fun generalKalman()
    {
        // NOTE:
        // high kalmanGain: measurements are accurate, estimates are unstable (big errors) *
        // low  kalmanGain: measurements are inaccurate, estimates are stable (small errors) **

        kalmanGain = errorInEstimate / (errorInEstimate + errorInDataMeasurement)
        // EQ 1:
        // 0 < KalmanGain < 1
        // use kalmanGain to place relative importance on errorInEstimate or errorInDataMeasurement
        // if kalmanGain is close to 1, measurement error is very small

        currentEstimate = previousEstimate + (kalmanGain * measuredValue - kalmanGain * previousEstimate)
        // EQ: 2
        // use kalmanGain to place relative importance on either measuredValue or previousEstimate
        // if kalmanGain is close to 0, currentEstimate will be closer to previousEstimate
        // and we are getting closer to the true value

        newErrorInEstimate = (1 - kalmanGain) * previousEstimate
        // EQ 3 (inverse of EQ1)
        // same as: newErrorInEstimate = (errorInDataMeasurement * previousEstimate) /
        //      (errorInDataMeasurement + previousEstimate)
        // if errorInDataMeasurement is very small we can rely more on the previousEstimate -> newErrorInEstimate decreases faster **
        // if errorInDataMeasurement is very large we must rely less on the previousEstimate -> newErrorInEstimate decreases slower *

    }


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
        return (abs(currAcc - prevAcc) < limit)

    }

    fun limitValues(newValues: MutableList<Float>, oldValues: MutableList<Float>,limit: Float): Boolean {
        return ((abs(newValues[0] - oldValues[0]) < limit)
                or (abs(newValues[1] - oldValues[1]) < limit)
                or (abs(newValues[2] - oldValues[2]) < limit))

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