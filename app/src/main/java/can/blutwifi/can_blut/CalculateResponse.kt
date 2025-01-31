package can.blutwifi.car_obd

import net.objecthunter.exp4j.ExpressionBuilder
import java.lang.Long
import java.util.Random


class CalculateResponse {


    fun calculate(mode: String, pidlenghtAdd: Int, bytes: String, formula: String): Float {
        var result: Float = kotlin.random.Random.nextFloat() * 10f
        try {
            val mutablelistOfVar = mutableListOf("A", "B", "C", "D", "E")
            val muatblelistOfPresentVar = mutableListOf<String>()
            val mutableMapOfVar = mutableMapOf<String, Double>()

            var i = 0
            MutableList(mutablelistOfVar.size) { index ->
                formula.contains(
                    mutablelistOfVar[index]
                )
            }.forEach { isvarPresent ->
                if (isvarPresent) {
                    mutableMapOfVar.put(
                        mutablelistOfVar[i],
                        Long.parseLong(bytes.substring(i * 3+pidlenghtAdd, i * 3 + 2+pidlenghtAdd), 16).toDouble()
                    )
                    muatblelistOfPresentVar.add(mutablelistOfVar[i])
                }
                i++
            }

            result =
                ExpressionBuilder(formula).variables(muatblelistOfPresentVar.toSet())
                    .build()
                    .setVariables(mutableMapOfVar).evaluate().toFloat()
        } catch (e: Exception) {
            result = 0f
        }
        //result=Long.parseLong(bytes.substring(0,2),16).toFloat()
        return result
    }

    fun calcNonNumericValues(bytes:String, typeofcalc: String):String
    {
        var result=""
        try {
            if (typeofcalc == "nonNumericEngineType") {

                val value = Integer.parseInt(bytes.substring(3, 5), 16).and(8)
                if (value == 0) {
                    result = "Spark ignition (e.g. Otto or Wankel engines)"
                } else if (value == 8) {
                    result = "Compression ignition (e.g. Diesel engines)"
                }
            } else if (typeofcalc == "nonNumericNumOFDTCCodes") {
                result = Integer.parseInt(bytes.substring(0, 2), 16).and(127).toString()
            } else if (typeofcalc == "nonNumericMILStatus") {
                val value = Integer.parseInt(bytes.substring(0, 2), 16).and(128)
                if (value == 0) {
                    result = "OFF"
                } else if (value == 128) {
                    result = "ON"
                }
            } else if (typeofcalc == "nonNumericFueltype") {
                val value = Integer.parseInt(bytes.substring(0, 2), 16)
                val mutablelist = mutableListOf(
                    "Not available",
                    "Gasoline",
                    "Methanol",
                    "Ethanol",
                    "Diesel",
                    "LPG",
                    "CNG",
                    "Propane",
                    "Electric",
                    "Bifuel running Gasoline",
                    "Bifuel running Methanol",
                    "Bifuel running Ethanol",
                    "Bifuel running LPG",
                    "Bifuel running CNG",
                    "Bifuel running Propane",
                    "Bifuel running Electricity",
                    "Bifuel running electric and combustion engine",
                    "Hybrid gasoline",
                    "Hybrid Ethanol",
                    "Hybrid Diesel",
                    "Hybrid Electric",
                    "Hybrid running electric and combustion engine",
                    "Hybrid Regenerative",
                    "Bifuel running diesel"
                )
                result = mutablelist[value]
            } else if (typeofcalc == "nonNumericFuelSysStat") {
                val value = Integer.parseInt(bytes.substring(0, 2), 16)
                val mutablelist = mutableListOf(
                    "The motor is off",
                    "Open loop due to insufficient engine temperature",
                    "Closed loop, using oxygen sensor feedback to determine fuel mix",
                    "Open loop due to engine load OR fuel cut due to deceleration",
                    "Open loop due to system failure",
                    "Closed loop, using at least one oxygen sensor but there is a fault in the feedback system"
                )
                result = mutablelist[value]
            } else if (typeofcalc == "nonNumericCommandSecAirSta") {
                val value = Integer.parseInt(bytes.substring(0, 2), 16)
                val mutablelist = mutableListOf(
                    "Upstream",
                    "Downstream of catalytic converter",
                    "From the outside atmosphere or off",
                    "Pump commanded on for diagnostics"
                )
                result = mutablelist[value]
            }

        }
        catch (e:Exception)
        {

        }
        return result
    }

    fun calcMultiframeNumeric(pidlenghtAdd: Int,bytes:String, formula: String):Float{

        var result=0f

       // try {
            val mutablelistOfVar = mutableListOf("A", "B", "C", "D", "E", "F","G","H","I","J","K","L","M","N","O","U","P","R","S","T","V","W","X","Y","Z")

            val muatblelistOfPresentVar = mutableListOf<String>()
            val mutableMapOfVar = mutableMapOf<String, Double>()
            val indexofendofbytesnum=bytes.indexOf(' ')+1
        val listofindexesofcolon= mutableListOf<Int>()
            var j=0

            bytes.forEach {
                if (it==':')
            {
                listofindexesofcolon.add(j)
            }
                j++
            }

            var bytes1=""
        listofindexesofcolon.add(bytes.length+3)
        var k=0
        while (k<listofindexesofcolon.size-1)
        {
            if (k==0) {
                bytes1+=bytes.substring(listofindexesofcolon[k]+8+pidlenghtAdd,listofindexesofcolon[k+1]-3)
            }
            else
            {
                bytes1+=bytes.substring(listofindexesofcolon[k]+1,listofindexesofcolon[k+1]-3)
            }
            k++
        }


            var i = 0
            MutableList(mutablelistOfVar.size) { index ->
                formula.contains(
                    mutablelistOfVar[index]
                )
            }.forEach { isvarPresent ->
                if (isvarPresent) {
                    mutableMapOfVar.put(
                        mutablelistOfVar[i],
                        Long.parseLong(bytes1.substring(i*3,i*3+2), 16).toDouble()
                    )
                    muatblelistOfPresentVar.add(mutablelistOfVar[i])
                }
                i++
            }

            result =
                ExpressionBuilder(formula).variables(muatblelistOfPresentVar.toSet())
                    .build()
                    .setVariables(mutableMapOfVar).evaluate().toFloat()
        /*} catch (e: Exception) {
            result = 0f
        }*/

        return result
    }

    fun calculateNumericwithMoreECUs (pidlenghtAdd: Int,bytes: String, formula: String ,ECUheader:kotlin.Long): Float
    {
        var result=0f
        try {
            var numberofresponses=1
            var flagfoundfirstspace=false

            var muatblelistOfResponsesEnds= mutableListOf(-2)
            var i=0
            var j=0
            bytes.forEach { char-> if (char<=' ' && !flagfoundfirstspace)
            {
                flagfoundfirstspace=true
            }
             else if (flagfoundfirstspace && char<=' ')
            {
                numberofresponses++
                muatblelistOfResponsesEnds.add(j-1)

            }else
            {
                flagfoundfirstspace=false
            }
                j++
            }

            muatblelistOfResponsesEnds.add(bytes.length)

            var listofnumberofFramefromtocalculate= mutableListOf<Int>()
            var bytestomultiframecalc=""

            var k = 0

            while (k < numberofresponses) {
                if (Long.parseLong(bytes.substring(0 + muatblelistOfResponsesEnds[k]+2, bytes.indexOf(' ') + muatblelistOfResponsesEnds[k]+2), 16) == ECUheader) {
                    listofnumberofFramefromtocalculate.add(k)
                    //start from ' ' (space)
                    if (listofnumberofFramefromtocalculate.size==1) {
                        bytestomultiframecalc += bytes.substring(pidlenghtAdd+
                            15 + muatblelistOfResponsesEnds[k]+2,
                            muatblelistOfResponsesEnds[k+1]
                        )
                    }
                    else
                    {
                        bytestomultiframecalc += bytes.substring(
                            6 + muatblelistOfResponsesEnds[k]+2,
                            muatblelistOfResponsesEnds[k+1]
                        )
                    }
                }

                k++
            }

            if (listofnumberofFramefromtocalculate.size==1) {
                result = calculate("", pidlenghtAdd, bytes.substring(13 +muatblelistOfResponsesEnds[listofnumberofFramefromtocalculate[0]]+2), formula)
            }
            else
            {
                result=calculateMultiFrameNumericwithMoreECUs(bytestomultiframecalc,formula)
            }

        }
        catch (e:Exception)
        {

        }

        return result
    }
    fun calculateMultiFrameNumericwithMoreECUs (bytes: String , formula: String ): Float
    {
        var result=0f
        try {
            val mutablelistOfVar = mutableListOf("B", "C", "D", "E", "F","G","H","I","J","K","L","M","N","O","U","P","R","S","T","V","W","X","Y","Z")

            val muatblelistOfPresentVar = mutableListOf<String>()
            val mutableMapOfVar = mutableMapOf<String, Double>()
            var i = 0
            MutableList(mutablelistOfVar.size) { index ->
                formula.contains(
                    mutablelistOfVar[index]
                )
            }.forEach { isvarPresent ->
                if (isvarPresent) {
                    mutableMapOfVar.put(
                        mutablelistOfVar[i],
                        Long.parseLong(bytes.substring(i*3+1,i*3+3), 16).toDouble()
                    )
                    muatblelistOfPresentVar.add(mutablelistOfVar[i])
                }
                i++
            }

            result =
                ExpressionBuilder(formula).variables(muatblelistOfPresentVar.toSet())
                    .build()
                    .setVariables(mutableMapOfVar).evaluate().toFloat()
        }
        catch (e:Exception)
        {

        }

        return result
    }


    fun calculateNonNumericwithMoreECUs (bytes: String, formula: String ,ECUheader:kotlin.Long): String {

        var result=""
       try {
            var numberofresponses=1
            var flagfoundfirstspace=false
            var numberofBytesinoneResponse=0
            var i=0
            var j=0
            bytes.forEach { char-> if (char<=' ' && !flagfoundfirstspace)
            {
                flagfoundfirstspace=true
            }
            else if (flagfoundfirstspace && char<=' ')
            {
                numberofresponses++
                if (i==0)
                {
                    numberofBytesinoneResponse=j-1
                }
                i++
            }else
            {
                flagfoundfirstspace=false
            }
                j++
            }
            if (numberofBytesinoneResponse==0)
            {
                numberofBytesinoneResponse=bytes.length
            }
            var listofnumberofFramefromtocalculate= mutableListOf<Int>()
            var bytestomultiframecalc=""

            var k = 0

            while (k < numberofresponses) {
                if (Long.parseLong(bytes.substring(0 + k * (numberofBytesinoneResponse+2), bytes.indexOf(' ') + k * (numberofBytesinoneResponse+2)), 16) == ECUheader) {
                    listofnumberofFramefromtocalculate.add(k)
                }

                k++
            }

            result = calcNonNumericValues( bytes.substring(13 + listofnumberofFramefromtocalculate[0] * (numberofBytesinoneResponse+2)), formula)


        }
        catch (e:Exception)
        {

        }
        return result
    }
}