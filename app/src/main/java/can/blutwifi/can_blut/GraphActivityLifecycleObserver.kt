package can.blutwifi.car_obd

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@SuppressLint("Range")
class GraphActivityLifecycleObserver(private val registry : ActivityResultRegistry, private val activity1: GraphsActivity)
    : DefaultLifecycleObserver {

    lateinit var launcher_file_access_permission: ActivityResultLauncher<String>
    lateinit var launcher_save_file: ActivityResultLauncher<String>
    val MESSAGE_START_SAVING=-3
    val MESSAGE_FINISH_SAVING=-4
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        launcher_file_access_permission= registry.register("1",owner, ActivityResultContracts.RequestPermission())
        {
            if (it==true)
            {
                launcher_save_file.launch("results.txt")
            }
        }
        launcher_save_file=registry.register("2",owner, ActivityResultContracts.CreateDocument("application/x-rawcc"))
        {uri->
            if (uri!=null)
                {
                    //activity1.handler1.obtainMessage(MESSAGE_START_SAVING).sendToTarget()
                    val outpuStraem=activity1.applicationContext.contentResolver.openOutputStream(uri)
                    var i=-1
                    var j=-1
                    var k=0

                    /*activity1.popuupWindow.isOutsideTouchable = true
                    activity1.popuupWindow.isFocusable = true
                    activity1.popuupWindow.showAsDropDown(activity1.activitybackground)*/

                    Thread{
        while (i< (MutableList(activity1.listOfValuesArrays.size+1){i->
                           if (i<activity1.listOfValuesArrays.size) {
                               activity1.listOfValuesArrays[i].size
                           }
                            else
                           {
                               activity1.listOfTimeValues[0].size
                           }}).min())
                   {
                       if (i==-1)
                       {
                           while (j<activity1.listOfValuesArrays.size)
                           {
                               if (j==-1)
                               {
                                   outpuStraem!!.write("Time[ms];".encodeToByteArray())
                               }
                               else
                               {
                                   outpuStraem!!.write((activity1.listOfGraphs[j]+";").encodeToByteArray())
                               }
                               j++
                           }
                           outpuStraem!!.write("\n".encodeToByteArray())

                       }
                       else
                       {

                           while (j < activity1.listOfValuesArrays.size) {
                               if (j==-1)
                               {
                                   outpuStraem!!.write((activity1.listOfTimeValues[0][i].toString()+";").encodeToByteArray())
                               }
                               else
                               {
                                   outpuStraem!!.write((activity1.listOfValuesArrays[j][i].toString()+";").encodeToByteArray())
                               }
                               j++
                           }
                           outpuStraem!!.write("\n".encodeToByteArray())

                       }

                       j=-1
                       i++
                   }
                        outpuStraem?.close()
                        activity1.runOnUiThread {activity1.handler1.obtainMessage(MESSAGE_FINISH_SAVING).sendToTarget() }
                }.start()

                }

        }


    }

    fun saveFile()
    {
        launcher_save_file.launch("results.txt")
        /*when {
            ContextCompat.checkSelfPermission(
                activity1,
                "android.permission.MANAGE_DOCUMENTS"
            ) == PackageManager.PERMISSION_GRANTED -> {
                launcher_save_file.launch("results")
            }

            else -> {
                launcher_file_access_permission.launch("android.permission.MANAGE_DOCUMENTS")
            }
        }*/

    }


    /*fun permissionSaveFIle()
    {
        launcher_file_access_permission.launch("android.permission.MANAGE_DOCUMENTS")
    }*/
}