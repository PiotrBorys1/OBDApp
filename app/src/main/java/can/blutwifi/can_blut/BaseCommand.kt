package can.blutwifi.car_obd

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Log
import com.pnuema.android.obd.models.PID
import com.pnuema.android.obd.statics.PersistentStorage
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import kotlin.system.measureTimeMillis


abstract class BaseCommand {

    protected var buffer: ArrayList<Int> = ArrayList()
    private var cmd: String? = null
     var bytecmd: ByteArray? = null
    private var useImperialUnits = false
    var rawData: String? = null

    companion object {
        const val NODATA = "NODATA"
        const val SEARCHING = "SEARCHING"
        const val DATA = "DATA"
        const val ELM327 = "ELM327"

        internal lateinit var mPid: PID

        private fun readPersistent() {
            if (mPid.isPersistent && PersistentStorage.containsPid(mPid)) {
                mPid.calculatedResult = PersistentStorage.getElement(mPid)?.calculatedResult ?: 0f
                mPid.calculatedResultString = PersistentStorage.getElement(mPid)?.calculatedResultString
                mPid.data = PersistentStorage.getElement(mPid)?.data ?: ArrayList()
            }
        }

        private fun storePersistent() {
            if (mPid.isPersistent && !PersistentStorage.containsPid(mPid)) {
                PersistentStorage.addElement(mPid)
            }
        }
    }


    abstract val formattedResult: String


    val rawResult: String
        get() {
            rawData = if (rawData == null || rawData!!.contains(SEARCHING) || rawData!!.contains(DATA) || rawData!!.contains(ELM327))
                NODATA
            else
                rawData

            return rawData!!
        }


    abstract val name: String

    @Suppress("RemoveEmptySecondaryConstructorBody")
    private constructor() {}


    constructor(command: String?) {
        this.cmd = command
    }

    constructor(command: String, pid: PID) : this(command.trim { it <= ' ' }) {
        mPid = pid
    }


    constructor(other: BaseCommand) : this(other.cmd)

    constructor(byteArray: ByteArray) {
        this.bytecmd=byteArray

    }

    protected abstract fun performCalculations()


    @Throws(IOException::class, InterruptedException::class)
    fun run(inputStream: InputStream, out: OutputStream): BaseCommand {

        sendCommand(out)

        readResult(inputStream)



        return this
    }


    @Throws(IOException::class, InterruptedException::class)
    private fun sendCommand(out: OutputStream) {
        // write to OutputStream (i.e.: a BluetoothSocket) with an added carriage return
        if (bytecmd!=null) {
            bytecmd!!.plus("\r".encodeToByteArray())
            out.write(bytecmd)
        }
        else {
            out.write((cmd!! + "\r").toByteArray())
        }
        out.flush()
    }

    @Throws(IOException::class)
    private fun readResult(inputStream: InputStream) {
        readRawData(inputStream)

    }

    /*private fun fillBuffer() {
        // read string each two chars
        //buffer.clear()
        rawDataByte= ByteArray(rawData!!.length)
        rawData?.chunked(1)?.forEach {
            try {
                rawDataByte!!.plus(it.encodeToByteArray())
            } catch (e: NumberFormatException) { return }
        }
    }*/

    @SuppressLint("SuspiciousIndentation")
    @Throws(IOException::class)
    private fun readRawData(inputStream: InputStream) {

         val reader = BufferedReader(InputStreamReader(inputStream))
         val res = StringBuilder()
         var c: Char
         var b = reader.read()
         while (b > -1) { // -1 if the end of the stream is reached
             c = b.toChar()
             if (c == '>') { // read until '>' arrives
                 break
             }
             res.append(c)
             b = reader.read()

         }
         rawData = res.toString().trim { it <= ' ' }
         rawData = rawData?.let {
             it.substring(it.indexOf(13.toChar()) + 1)
         }

    }


    fun useImperialUnits(): Boolean {
        return useImperialUnits
    }

    fun setImperialUnits(isImperial: Boolean) {
        this.useImperialUnits = isImperial
    }
}