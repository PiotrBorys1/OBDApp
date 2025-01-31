package can.blutwifi.car_obd

import android.bluetooth.BluetoothSocket
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Message
import com.pnuema.android.obd.commands.BaseObdCommand
import com.pnuema.android.obd.commands.OBDCommand
import com.pnuema.android.obd.enums.ObdModes
import com.pnuema.android.obd.models.PID
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket
import java.util.logging.Handler

class ThreadReadWrite(var custom_command_to_vehicle:String?, var pid:PID?, var socketWIFI: Socket?, var socket:BluetoothSocket?, var handler:android.os.Handler, var additionalvartoRepeatThread:Int):Thread() {

    val MESSAGE_OBD_RESULT: Int = 5
    val MESSAGE_OBD_WRONG_RESULT: Int = 6
    val MESSAGE_RESPONSE_CYCLIC_PASSWORD: Int = 9
    val MESSAGE_RESPONSE_PASSWORD_CHANGE: Int = 10
    val MESSAGE_RESPONSE_ELM237_COMMAND: Int = 11
    val MESSAGE_RESPONSE_STREAM_VIDEO: Int = 13
    lateinit var obdcommand:CustomOBDCommand
    lateinit var standardOBDCommand:OBDCommand
    var rawOBDResult:String=""
    var rawOBDResultByte:ByteArray?=null
    lateinit var reader:BufferedReader
    var imageBitmap:Bitmap?=null

    override fun run() {
        super.run()

        if (pid==null)
        {
           if (custom_command_to_vehicle!!.contains("AT")) {
               try {

                   if (socket!=null) {
                       socket!!.outputStream.write((custom_command_to_vehicle!! + "\r").encodeToByteArray())
                       socket!!.outputStream.flush()
                       reader = BufferedReader(InputStreamReader(socket!!.inputStream))
                   }
                   else
                   {
                       socketWIFI!!.outputStream.write((custom_command_to_vehicle!! + "\r").encodeToByteArray())
                       socketWIFI!!.outputStream.flush()
                       reader = BufferedReader(InputStreamReader(socketWIFI!!.inputStream))
                   }

                   val res = StringBuilder()

                   var c: Char
                   var b = reader.read()
                   while (b > -1) {
                       c = b.toChar()

                       if (c == '>') {
                           break
                       }
                       res.append(c)
                       b = reader.read()
                   }

                   val Msg = Message.obtain(
                       handler,
                       MESSAGE_RESPONSE_ELM237_COMMAND, 0, 0, res.toString().trim { it <= ' ' }
                   )
                   handler.sendMessage(Msg)
               } catch (e: Exception) {
                   val Msg = Message.obtain(
                       handler,
                       MESSAGE_OBD_WRONG_RESULT, additionalvartoRepeatThread, 0
                   )
                   handler.sendMessage(Msg)
               }
           }
            else {

               try {
                   obdcommand = CustomOBDCommand(custom_command_to_vehicle!!)
                   if (socket!=null) {
                       obdcommand.run(socket!!.inputStream, socket!!.outputStream)
                   }
                   else
                   {
                       obdcommand.run(socketWIFI!!.inputStream, socketWIFI!!.outputStream)
                   }
                   rawOBDResult=obdcommand.rawResult

                val Msg = Message.obtain(
                           handler,
                           MESSAGE_OBD_RESULT, additionalvartoRepeatThread, 0, rawOBDResult
                       )
                       handler.sendMessage(Msg)

              } catch (e: java.lang.Exception) {

                   val Msg = Message.obtain(
                       handler,
                       MESSAGE_OBD_WRONG_RESULT, additionalvartoRepeatThread, 0,""
                   )
                   handler.sendMessage(Msg)

               }
           }
        }
        else
        {
            try {
                standardOBDCommand = OBDCommand(pid!!)
                standardOBDCommand.setIgnoreResult(true)
                if (socket!=null) {
                    standardOBDCommand.run(socket!!.inputStream, socket!!.outputStream)
                }
                else
                {
                    standardOBDCommand.run(socketWIFI!!.inputStream, socketWIFI!!.outputStream)
                }
                rawOBDResult = standardOBDCommand.rawResult

                    val Msg = Message.obtain(
                        handler,
                        MESSAGE_OBD_RESULT, 0, 0, rawOBDResult
                    )
                    handler.sendMessage(Msg)

            }
            catch (e:java.lang.Exception)
            {

                val Msg = Message.obtain(handler,
                    MESSAGE_OBD_WRONG_RESULT, additionalvartoRepeatThread, 0,""
                )
                handler.sendMessage(Msg)

            }
        }

    }


    fun cancel() {
        try {
            socket!!.close()
        } catch (e: IOException) {

        }
    }
}