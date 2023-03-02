package com.example.combobackup.mqtt

class DefineCommands
{
    val RequestMainData: String = intToHexStr(0x11)

    val TempWarningCmd: String = intToHexStr(0x21)
    val TempCmd: String = intToHexStr(0x22)

    val GateDataCmd: String = intToHexStr(0x23)
    val AutoAtten: String = intToHexStr(0x24)

    val OvfFlagCmd: String = intToHexStr(0x25)
    val SyncFlagCmd: String = intToHexStr(0x26)

    val SweepTimdCmd: String = intToHexStr(0x30)

    val ReadyCmd: String = intToHexStr(0x200000)

    val ResponceForConfigErr: String = intToHexStr(0x98)
    val ResponseForConfig: String = intToHexStr(0x99)

    val RestartConfig: String = intToHexStr(0x44)
    val ForceTurnOff: String = intToHexStr(0x45)

    val ChangeMode: String = intToHexStr(0x65)
    val ChangeToVSWR: String = intToHexStr(0x00)
    val ChangeToSA: String = intToHexStr(0x01)
    val ChangeTo5G: String = intToHexStr(0x02)
    val ChangeToLte: String = intToHexStr(0x03)

    val NrToSaCommand: String = intToHexStr(0x66)

    val ChangeClockSourceCmd: String = intToHexStr(0x50)
    val LockStatusCmd: String = intToHexStr(0x51)
    val GPSInfoRequestCmd: String = intToHexStr(0x52)
    val GPSInfoResponseCmdVal = 0x53

    val GPSHoldoverRequestCmdVal = 0x54
    val GPSHoldoverRequestCmd: String = intToHexStr(0x54)

    val RequestTaeMeasure: String = intToHexStr(0x71)
    val ReadyTaeMeasure: String = intToHexStr(0x72)

    fun intToHexStr(i: Int): String {
        return "0x" + String.format("%2s", Integer.toHexString(i)).replace(' ', '0')
    }
}
