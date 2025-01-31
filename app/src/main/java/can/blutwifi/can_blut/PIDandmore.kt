package can.blutwifi.car_obd

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class PIDandmore(@SerialName("Mode") val Mode:String?=null,
                      @SerialName("PID") val PID:String?=null,
                      @SerialName("Bytes") val Bytes:String?=null,
                      @SerialName("Description") val Description:String?=null,
                      @SerialName("Min") val Min:String?=null,
                      @SerialName("Max") val Max:String?=null,
                      @SerialName("Units") val Units:String="",
                      @SerialName("Formula") val Formula:String?=null)

{
}