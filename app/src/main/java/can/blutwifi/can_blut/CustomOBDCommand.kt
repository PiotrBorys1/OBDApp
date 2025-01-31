package can.blutwifi.car_obd



class CustomOBDCommand(var command:String):BaseCommand(command) {
    override val formattedResult: String
        get() = ""
    override val name: String
        get() = ""

    override fun performCalculations() {

    }
}