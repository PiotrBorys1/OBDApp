package can.blutwifi.car_obd

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import com.pnuema.android.obd.enums.ObdModes
import com.pnuema.android.obd.models.PID

class ThreadSendGenralDiagnostic(val activity:ActivityDiagnostics):Thread() {
    val FINISH_SEND_THREAD=-99
    val NEXT_MESSAGE_SEND_THREAD=-100
    val MESSAGE_CHANGE_MODE_01_02=-101
    val FIRST_MESSAGE_AFTER_CHANGE_OF_MODE=-102
    var numberOfFreezeFrametoAskFromActiv=0
    lateinit var handlerThread:Handler
    override fun run() {
        super.run()
        var i=0
        ThreadReadWrite("",activity.listOfPIDs[i],activity.SocketWIFI, activity.mmSocket,activity.handler1,0).start()
        activity.actualsendCommnad = i
        i++
        if (i>=activity.listOfPIDs.size)
        {
            i=0
        }
        Looper.prepare()
        handlerThread=object :Handler(Looper.myLooper()!!)
        {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if(msg.what ==FIRST_MESSAGE_AFTER_CHANGE_OF_MODE)
                {
                    try {
                        i=0
                        activity.actualsendCommnad = i
                        ThreadReadWrite(
                            "",
                            activity.listOfPIDs[0],
                            activity.SocketWIFI,
                            activity.mmSocket,
                            activity.handler1,
                            0
                        ).start()

                        i++
                        if (i >= activity.listOfPIDs.size) {
                            i = 0
                        }
                    }
                    catch (e:Exception)
                    {}


                }
                else if (msg.what == NEXT_MESSAGE_SEND_THREAD) {
                    while (true) {
                        try {
                            if (i > 0) {

                                if (activity.listOfPIDs.subList(0, i)
                                        .count { PID -> PID == activity.listOfPIDs[i] } == 0
                                ) {
                                    break
                                }
                            } else {
                                break
                            }
                            i++
                            if (i > activity.listOfPIDs.size - 1) {
                                i = 0
                                break
                            }
                        }
                        catch (e:Exception)
                        {
                            i=0
                            break
                        }
                    }
                    activity.actualsendCommnad = i
                    ThreadReadWrite(
                        "",
                        activity.listOfPIDs[i],
                        activity.SocketWIFI,
                        activity.mmSocket,
                        activity.handler1,
                        0
                    ).start()

                    // activity.readwriteThreadFinised=false
                    //while (!activity.flagcalculationsENdCanincrementacualcommandnumber) {
                    //}

                    //activity.flagcalculationsENdCanincrementacualcommandnumber=false
                    i++
                    if (i >= activity.listOfPIDs.size) {
                        i = 0
                    }
                }
                else if (msg.what==MESSAGE_CHANGE_MODE_01_02)
                {
                    //Looper.myLooper()!!.quit()
                    activity.handler1.sendMessage(Message.obtain(activity.handler1,MESSAGE_CHANGE_MODE_01_02))

                }

            }

        }


        Looper.loop()

    }
}