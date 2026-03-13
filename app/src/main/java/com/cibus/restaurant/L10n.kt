package com.cibus.restaurant

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private const val PREF_LANG = "app_lang_restaurant"
private const val LANG_EN = "EN"
private const val LANG_URDU = "اردو"

object AppLanguage {
    const val EN = LANG_EN
    const val ROMAN_URDU = LANG_URDU
    val all = listOf(EN, ROMAN_URDU)
}

fun Context.getAppLang(): String = getSharedPreferences("cibus_restaurant", Context.MODE_PRIVATE).getString(PREF_LANG, LANG_EN) ?: LANG_EN
fun Context.setAppLang(lang: String) = getSharedPreferences("cibus_restaurant", Context.MODE_PRIVATE).edit().putString(PREF_LANG, lang).apply()

object ResL10n {
    fun isUrdu(context: Context) = context.getAppLang() == LANG_URDU

    fun loginTitle(ctx: Context) = "Cibus Restaurant"
    fun loginSubtitle(ctx: Context) = if (isUrdu(ctx)) "Restaurant manage karo" else "Manage your restaurant"
    fun email(ctx: Context) = "Email"
    fun password(ctx: Context) = "Password"
    fun signIn(ctx: Context) = if (isUrdu(ctx)) "Login" else "Sign In"
    fun applyLink(ctx: Context) = if (isUrdu(ctx)) "Partner bano" else "Apply to partner"

    fun applyTitle(ctx: Context) = if (isUrdu(ctx)) "Cibus ke sath judo" else "Join Cibus"
    fun applyHint(ctx: Context) = if (isUrdu(ctx)) "Zaroori: NTN, PFA license, CNIC" else "Need: NTN, PFA license, CNIC"
    fun ownerName(ctx: Context) = if (isUrdu(ctx)) "Malik ka naam" else "Owner name"
    fun restaurantName(ctx: Context) = if (isUrdu(ctx)) "Dhabe / restaurant ka naam" else "Restaurant name"
    fun address(ctx: Context) = if (isUrdu(ctx)) "Pata" else "Address"
    fun city(ctx: Context) = if (isUrdu(ctx)) "Shehar" else "City"
    fun phone(ctx: Context) = "Phone"
    fun cnic(ctx: Context) = if (isUrdu(ctx)) "CNIC (13 numbers)" else "CNIC (13 numbers)"
    fun ntn(ctx: Context) = "NTN"
    fun pfaLicense(ctx: Context) = if (isUrdu(ctx)) "PFA license number" else "PFA license number"
    fun submit(ctx: Context) = if (isUrdu(ctx)) "Bhejo" else "Submit"
    fun successMsg(ctx: Context) = if (isUrdu(ctx)) "Apply ho gaya. Hum call karenge." else "Done! We will call you."

    fun errBasic(ctx: Context) = if (isUrdu(ctx)) "Sab zaroori fields bharo" else "Fill all fields"
    fun errPhone(ctx: Context) = if (isUrdu(ctx)) "10+ digit phone likho" else "Enter 10+ digit phone"
    fun errCnic(ctx: Context) = if (isUrdu(ctx)) "CNIC 13 numbers hona chahiye" else "CNIC must be 13 digits"
    fun errNtnPfa(ctx: Context) = if (isUrdu(ctx)) "NTN aur PFA license likho" else "Enter NTN and PFA license"
}
