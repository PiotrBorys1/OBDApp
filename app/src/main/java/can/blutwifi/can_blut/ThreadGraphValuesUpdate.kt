package can.blutwifi.car_obd

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import com.pnuema.android.obd.models.PID

class ThreadGraphValuesUpdate(val activity:GraphsActivity, val listofCommands:MutableList<PID>):Thread() {
    val FINISH_SEND_THREAD = -99
    val NEXT_MESSAGE_SEND_THREAD = -100
    lateinit var handlerThread: Handler
    override fun run() {
        super.run()
        var i = 0
        var sleeptime = 0L
        ThreadReadWrite(
            "",
            listofCommands[i],
            activity.SocketWIFI,
            activity.mmSocket,
            activity.handler1,
            0
        ).start()

        activity.accuallysendCommandToVehicle = i
        i++
        if (i > listofCommands.size - 1) {
            i = 0
            sleeptime = (activity.SampleTime / activity.listOfValuesArrays.size).toLong() - 20L
            if (sleeptime > 0) {
                sleep(sleeptime)
            }
        }
        Looper.prepare()
        handlerThread = object : Handler(Looper.myLooper()!!) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == NEXT_MESSAGE_SEND_THREAD) {

                    while (true) {
                        if (i > 0) {
                            if (activity.ListOFPID.subList(0, i)
                                    .count { PID -> PID == activity.ListOFPID[i] } == 0
                            ) {
                                break
                            }
                        } else {
                            break
                        }
                        i++
                        if (i > activity.ListOFPID.size - 1) {
                            i = 0
                            break
                        }
                    }
                    ThreadReadWrite(
                        "",
                        listofCommands[i],
                        activity.SocketWIFI,
                        activity.mmSocket,
                        activity.handler1,
                        0
                    ).start()
                    //activity.readwriteThreadFinised=false
                    //while (!activity.flagcalculationsENdCanincrementacualcommandnumber) {
                    //}
                    activity.accuallysendCommandToVehicle = i
                    i++
                    if (i > listofCommands.size - 1) {
                        i = 0
                        sleeptime =
                            (activity.SampleTime / activity.listOfValuesArrays.size).toLong() - 20L
                        if (sleeptime > 0) {
                            sleep(sleeptime)
                        }
                    }
                } else if (msg.what == FINISH_SEND_THREAD) {
                    Looper.myLooper()!!.quit()
                }

            }
        }



        Looper.loop()

    }
}