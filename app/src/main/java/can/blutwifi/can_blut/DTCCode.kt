package can.blutwifi.car_obd

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class DTCCode(
    @SerialName("code")
    val code:String="",
    @SerialName("description")
    val description:String=""
)
{
}