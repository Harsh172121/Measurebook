package com.example.util

object MeasurementFields {
    // Field definitions with keys, English names, and Gujarati names
    data class FieldInfo(
        val key: String,
        val english: String,
        val gujarati: String
    )

    val BLOUSE_FIELDS = listOf(
        FieldInfo("blouseLength", "Blouse Length", "બ્લાઉઝ લંબાઈ"),
        FieldInfo("upperBust", "Upper Bust", "છાતીનો ઉપરનો ભાગ"),
        FieldInfo("bust", "Bust", "છાતી"),
        FieldInfo("waist", "Waist", "કમર"),
        FieldInfo("sleeveLength", "Sleeve Length", "બાયની લંબાઈ"),
        FieldInfo("sleeveRound", "Sleeve Round", "બાયની ગોલાઈ"),
        FieldInfo("armRound", "Arm Round", "બાવડાની ગોલાઈ"),
        FieldInfo("armhole", "Armhole", "મુંઢો (આર્મહોલ)"),
        FieldInfo("shoulder", "Shoulder", "શોલ્ડર"),
        FieldInfo("neckFront", "Front Neck", "આગળનું ગળું"),
        FieldInfo("neckBack", "Back Neck", "પાછળનું ગળું")
    )

    val PUNJABI_TOP_FIELDS = listOf(
        FieldInfo("topLength", "Top Length", "ટોપ લંબાઈ"),
        FieldInfo("topUpperBust", "Upper Bust", "છાતીનો ઉપરનો ભાગ"),
        FieldInfo("topBust", "Bust", "છાતી"),
        FieldInfo("topWaist", "Waist", "કમર"),
        FieldInfo("topHips", "Hips (Seat)", "સીટ (હીપ્સ)"),
        FieldInfo("topShoulder", "Shoulder", "શોલ્ડર"),
        FieldInfo("topSleeveLength", "Sleeve Length", "બાયની લંબાઈ"),
        FieldInfo("topSleeveRound", "Sleeve Round", "બાયની ગોલાઈ"),
        FieldInfo("topArmRound", "Arm Round", "બાવડાની ગોલાઈ"),
        FieldInfo("topArmhole", "Armhole", "મુંઢો (આર્મહોલ)")
    )

    val SALWAR_PANT_FIELDS = listOf(
        FieldInfo("salwarLength", "Salwar Length", "સલવાર લંબાઈ"),
        FieldInfo("salwarWaist", "Waist", "કમર"),
        FieldInfo("salwarHips", "Hips", "સીટ (હીપ્સ)"),
        FieldInfo("mori", "Mori (Bottom)", "મોરી (પાયચો)"),
        FieldInfo("jaang", "Thigh (Jaang)", "જાંગ"),
        FieldInfo("dhichan", "Knee (Dhichan)", "ઢીંચણ")
    )

    fun getFieldLabel(key: String, language: String): String {
        val field = (BLOUSE_FIELDS + PUNJABI_TOP_FIELDS + SALWAR_PANT_FIELDS).firstOrNull { it.key == key }
        return if (field != null) {
            "${field.english} (${field.gujarati})"
        } else {
            key
        }
    }
}
