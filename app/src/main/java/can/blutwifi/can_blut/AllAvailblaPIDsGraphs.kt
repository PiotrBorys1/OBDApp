package can.blutwifi.car_obd

import com.pnuema.android.obd.models.DTC
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AllAvailblaPIDsGraphs(
    @SerialName("pids")
    val pids: List<PIDandmore> = ArrayList()
) {
}