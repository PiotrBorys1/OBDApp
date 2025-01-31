package can.blutwifi.car_obd

import android.annotation.SuppressLint
import java.lang.Long

class ParseErrors {



    fun parseGenericErrors1(bytes:String): MutableList<MutableList<String>>
    {
        val mutablelistofMutableListofFaults= mutableListOf<MutableList<String>>()
        val mutablelistofECUHeaders= mutableListOf<kotlin.Long>()
        val mutablelistofcheckifECUismultiline=mutableListOf<Boolean>(false)
        val mutablelistofECUmessageEnd= mutableListOf(-2)
        var ECUheader:kotlin.Long
        var sequencenumberofECU:Int
        var flagfoundfirstspace=false
        var actualprocessedECUHeader:kotlin.Long
        var listOfUnfinishedErrorsString= mutableListOf<kotlin.String>()
        var listOfAlreadyProcessedECUHeaders= mutableListOf<kotlin.Long>()
        val tabletoParsingFault = mutableMapOf(
            0 to "P",
            1 to "C",
            2 to "B",
            3 to "U"
        )
      try {

               var i=0
               var j=0
               mutablelistofECUHeaders.add(Long.parseLong(bytes.substring(0,bytes.indexOf(' ')),16))
               mutablelistofMutableListofFaults.add(mutableListOf())
               bytes.forEach { char-> if (char<=' ' && !flagfoundfirstspace)
               {
                   flagfoundfirstspace=true
               }
               else if (flagfoundfirstspace && char<=' ')
               {
                       mutablelistofECUmessageEnd.add(j-1)
                       ECUheader=Long.parseLong(bytes.substring(j+1,j+1+bytes.substring(j+1).indexOf(' ')),16)
                       if (!mutablelistofECUHeaders.contains(ECUheader))
                       {
                           mutablelistofECUHeaders.add(ECUheader)
                           mutablelistofECUHeaders.sort()
                           mutablelistofMutableListofFaults.add(mutableListOf())
                           mutablelistofcheckifECUismultiline.add(mutablelistofECUHeaders.indexOf(ECUheader),false)
                       }
                       else
                       {
                           mutablelistofcheckifECUismultiline[mutablelistofECUHeaders.indexOf(ECUheader)]=true
                       }

               }else
               {
                   flagfoundfirstspace=false
               }
                   j++
               }
            mutablelistofECUmessageEnd.add(bytes.length)
          listOfUnfinishedErrorsString= MutableList(mutablelistofcheckifECUismultiline.size){""}



        //activity.titleError.text=mutablelistofcheckifECUismultiline.size.toString()+" "+mutablelistofcheckifECUismultiline[0].toString()//mutablelistofECUmessageEnd.size.toString()+" "+mutablelistofECUmessageEnd[2]+" "+bytes.length.toString()
            var l=0
            var m:Int
            var stringunderConctruction:String
          while (l<mutablelistofECUmessageEnd.size-1)
          {
              actualprocessedECUHeader=Long.parseLong(bytes.substring(mutablelistofECUmessageEnd[l]+2,mutablelistofECUmessageEnd[l]+2+bytes.substring(mutablelistofECUmessageEnd[l]+2).indexOf(' ')),16)
              sequencenumberofECU=mutablelistofECUHeaders.indexOf(actualprocessedECUHeader)

              if (mutablelistofcheckifECUismultiline[sequencenumberofECU]) {
                  if (!listOfAlreadyProcessedECUHeaders.contains(actualprocessedECUHeader)) {
                      m = mutablelistofECUmessageEnd[l] + 18
                  }
                  else
                  {
                      m = mutablelistofECUmessageEnd[l] + 9
                  }
              }
              else
              {
                  m = mutablelistofECUmessageEnd[l] + 15
              }
              listOfAlreadyProcessedECUHeaders.add(actualprocessedECUHeader)
              var numberOfLoopLoops=0
              while (m<mutablelistofECUmessageEnd[l+1])
              {
                  if (numberOfLoopLoops==0 && listOfAlreadyProcessedECUHeaders.contains(actualprocessedECUHeader) && listOfUnfinishedErrorsString[sequencenumberofECU]!="")
                  {
                      stringunderConctruction =tabletoParsingFault[(((Long.parseLong(
                          listOfUnfinishedErrorsString[sequencenumberofECU].substring(0 , 1),
                          16
                      )).and(12L)).shr(2)).toInt()]!!
                      stringunderConctruction += (
                              java.lang.Long.parseLong(
                                  listOfUnfinishedErrorsString[sequencenumberofECU].substring(
                                      0,
                                     1
                                  ), 16
                              )).and(3L).toString() +
                              (Long.parseLong(listOfUnfinishedErrorsString[sequencenumberofECU].substring(1, 2), 16)).toString() +
                          (Long.parseLong(
                              bytes.substring(m , m+ 1),
                              16
                          )).toString() +
                                  (Long.parseLong(bytes.substring(m + 2, m  + 3), 16)).toString()

                      mutablelistofMutableListofFaults[sequencenumberofECU].add(
                          stringunderConctruction
                      )
                      listOfUnfinishedErrorsString[sequencenumberofECU]=""
                  }
                  else if (m<mutablelistofECUmessageEnd[l+1]-2) {
                      stringunderConctruction = tabletoParsingFault[(((Long.parseLong(
                          bytes.substring(m , m + 1),
                          16
                      )).and(12L)).shr(2)).toInt()]!!
                      stringunderConctruction += (
                              java.lang.Long.parseLong(
                                  bytes.substring(
                                      m  ,
                                      m  +1
                                  ), 16
                              )).and(3L).toString() +
                              (Long.parseLong(bytes.substring(m + 1, m+ 2), 16)).toString() +
                              (Long.parseLong(
                                  bytes.substring(m  + 3, m+ 4),
                                  16
                              )).toString() +
                              (Long.parseLong(bytes.substring(m + 4, m  + 5), 16)).toString()

                      mutablelistofMutableListofFaults[sequencenumberofECU].add(
                          stringunderConctruction
                      )
                  }
                  else if (m>=mutablelistofECUmessageEnd[l+1]-2)
                  {
                      listOfUnfinishedErrorsString[sequencenumberofECU]=
                          bytes.substring(
                              m,
                              m + 2
                          )

                  }
                  m+=6
                  numberOfLoopLoops++
              }
              l++
          }


        }
        catch (e:java.lang.Exception)
        {

        }
        return mutablelistofMutableListofFaults
    }



    fun findErrorsCustom(bytes:String,offset:Int): MutableList<MutableList<String>>
    {
        val tabletoParsingFault = mutableMapOf(
            0 to "P",
            1 to "C",
            2 to "B",
            3 to "U"
        )
        var mutablelistofMutableListofFaults = mutableListOf<MutableList<String>>()
        try {

            val listFOHeaders = mutableMapOf<kotlin.Long, Boolean>()
            var nextHeader = 0L
            var flagfoundfirstspace = false
            val muatblelistOfResponsesStart = mutableListOf<Int>(0)
            var j = 0
            bytes.forEach { char ->
                if (char <= ' ' && !flagfoundfirstspace) {
                    flagfoundfirstspace = true
                } else if (flagfoundfirstspace && char <= ' ') {
                    muatblelistOfResponsesStart.add(j + 1)
                } else {
                    flagfoundfirstspace = false
                }
                j++
            }
            muatblelistOfResponsesStart.forEach { startofresp ->
                nextHeader = java.lang.Long.parseLong(
                    bytes.substring(
                        startofresp,
                        startofresp + bytes.indexOf(' ')
                    ), 16
                )
                listFOHeaders[nextHeader] =
                    !(listFOHeaders.isEmpty() || !listFOHeaders.containsKey(nextHeader))
            }
            val MutableListOFString = MutableList(listFOHeaders.size) { index -> "" }
            val headers = listFOHeaders.keys.toMutableList()
            headers.sort()
            muatblelistOfResponsesStart.add(bytes.length + 1)
            var i = 0
            while (i < muatblelistOfResponsesStart.size - 1) {
                nextHeader = java.lang.Long.parseLong(
                    bytes.substring(
                        muatblelistOfResponsesStart[i],
                        muatblelistOfResponsesStart[i] + bytes.indexOf(' ')
                    ), 16
                )
                if (listFOHeaders[nextHeader]!!) {
                    if (MutableListOFString[headers.indexOf(nextHeader)].isEmpty()) {
                        MutableListOFString[headers.indexOf(nextHeader)] = bytes.substring(
                            muatblelistOfResponsesStart[i] + 16 + offset,
                            muatblelistOfResponsesStart[i + 1] - 1
                        )
                    } else {
                        MutableListOFString[headers.indexOf(nextHeader)] += bytes.substring(
                            muatblelistOfResponsesStart[i] + 7,
                            muatblelistOfResponsesStart[i + 1] - 1
                        )
                    }
                } else {
                    MutableListOFString[headers.indexOf(nextHeader)] = bytes.substring(
                        muatblelistOfResponsesStart[i] + 13 + offset,
                        muatblelistOfResponsesStart[i + 1] - 1
                    )
                }
                i++
            }

            var m = 0
            var k = 0
            mutablelistofMutableListofFaults =
                MutableList(MutableListOFString.size) { mutableListOf() }
            var stringunderConctruction = ""
            MutableListOFString.forEach { string ->
                while (m < string.length - 1) {
                    stringunderConctruction =
                        tabletoParsingFault[(((Long.parseLong(
                            string.substring(m , m + 1),
                            16
                        )).and(12L)).shr(2)).toInt()]!!
                    stringunderConctruction += (
                            java.lang.Long.parseLong(
                                string.substring(
                                    m  ,
                                    m  +1
                                ), 16
                            )).and(3L).toString() +
                            (Long.parseLong(string.substring(m + 1, m+ 2), 16)).toString() +
                            (Long.parseLong(
                                string.substring(m  + 3, m+ 4),
                                16
                            )).toString() +
                            (Long.parseLong(string.substring(m + 4, m  + 5), 16)).toString()

                    mutablelistofMutableListofFaults[k].add(stringunderConctruction
                    )
                    m += 6
                }
                m = 0
                k++
            }
        }
        catch(e:Exception)
        {

        }
        return mutablelistofMutableListofFaults



    }


}