package can.blutwifi.car_obd

import java.lang.Long

class CalculateGeneralInfo {
    fun calculate(mode:String, pid:String, bytes:String, numberOfVarInOrder:Int): String
    {
        var result:String=""
        when (mode)
        {
            "41"->{
                when(pid)
                {
                }
            }
            else->{result=""}
        }


        return result
    }
}