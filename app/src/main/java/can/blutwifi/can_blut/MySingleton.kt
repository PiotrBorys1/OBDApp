package can.blutwifi.car_obd

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import javax.net.ssl.SSLServerSocketFactory

class MySingleton private constructor() {

    companion object {


        @Volatile
        private var instance: MySingleton? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: MySingleton().also { instance = it }
            }
    }
    var BlutSocket:BluetoothSocket?=null
    var wifiIP:String?=null
    var SocketPort:Int=9999
    var SocketWIFI:Socket?=null
    var BludDevice:BluetoothDevice?=null
    var MainActivity:MainActivity?=null
    var numberOfDetectedECUs:Int=1
    var VideoImageBytearray:ByteArray?=null
    var numberofBytesInAusioByteArray=10000
    var audioByteArraytoPlay=ByteArray(numberofBytesInAusioByteArray)
}