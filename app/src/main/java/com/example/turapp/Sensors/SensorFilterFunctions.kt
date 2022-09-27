package com.example.turapp.Sensors

/*
    From lecture

    Low-pass
    Yn=B*Y2+(1-B)y
    (Gravity new) = alpha * (gravity old) + (1-alpha) * event.values
    May also be applied to other sensors, e.g. acceleration

    High-pass
    Y = a*y1 +a*(x2-x1)
    (Gravity new) = alpha*(gravity old) + alpha*(event.values old –
    event.values new)

*/


private  const val LOW_VALUES_THRESHOLD_ACC = 0.005
private  const val HIGH_VALUES_THRESHOLD_ACC = 30.50

class SensorFilterFunctions {

    fun cleanSomeSensorData(data: List<Float>): List<Float> {
        // Clean inputdata in a function

        return data
    }

    fun lowValuesFilter(reading: List<Float>): MutableList<Float> {
        // filter sensor data
        val data : MutableList<Float> = ArrayList()
        val iterator = reading.listIterator()

        iterator.forEach {
            if(it < LOW_VALUES_THRESHOLD_ACC) {
                data.add(it)
                //println(it)
            }
            else {
//                data.add(LOW_VALUES_THRESHOLD_ACC)
            }
        }

        return data
    }

    fun highValuesAccFilter(data: MutableList<Float>): List<Float> {
        // filter sensor data
        val iterator = data.listIterator()

        iterator.forEach {
            if(it < HIGH_VALUES_THRESHOLD_ACC)
                iterator.remove()
            //println(it)

        }

        return data
    }



}