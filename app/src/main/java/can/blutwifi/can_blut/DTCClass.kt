package can.blutwifi.car_obd

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class DTCClass(
    @SerialName ("dtcs")
    val listOfDTC:MutableList<DTCCode> = ArrayList()
) {

}