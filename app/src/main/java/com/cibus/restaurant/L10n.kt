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

    fun loginTitle(ctx: Context) = "HUBB Merchant"
    fun loginSubtitle(ctx: Context) = if (isUrdu(ctx)) "Restaurant manage karo" else "Manage your restaurant"
    fun email(ctx: Context) = "Email"
    fun password(ctx: Context) = "Password"
    fun signIn(ctx: Context) = if (isUrdu(ctx)) "Login" else "Sign In"
    fun signInScreenTitle(ctx: Context) = signIn(ctx)
    fun back(ctx: Context) = if (isUrdu(ctx)) "Wapas" else "Back"
    fun registerNewRestaurant(ctx: Context) = if (isUrdu(ctx)) "Naya restaurant register karo" else "Register new restaurant"
    fun loginNeedEmailPassword(ctx: Context) = if (isUrdu(ctx)) "Email aur password likho" else "Please enter email and password"
    fun sessionVerifyFailed(ctx: Context) = if (isUrdu(ctx)) "Session verify nahi ho saki. Dobara koshish karein." else "Could not verify your session. Try again."
    fun applyLink(ctx: Context) = if (isUrdu(ctx)) "Partner bano" else "Apply to partner"

    fun applyTitle(ctx: Context) = if (isUrdu(ctx)) "HUBB ke sath judo" else "Join HUBB"
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
    fun save(ctx: Context) = if (isUrdu(ctx)) "Save karein" else "Save"
    fun add(ctx: Context) = if (isUrdu(ctx)) "Jodein" else "Add"
    fun successMsg(ctx: Context) = if (isUrdu(ctx)) "Application submit ho gayi. Hum contact karenge." else "Application submitted – we will contact you."

    fun errBasic(ctx: Context) = if (isUrdu(ctx)) "Sab zaroori fields bharo" else "Fill all fields"
    fun errPhone(ctx: Context) = if (isUrdu(ctx)) "10+ digit phone likho" else "Enter 10+ digit phone"
    fun errCnic(ctx: Context) = if (isUrdu(ctx)) "CNIC 13 numbers hona chahiye" else "CNIC must be 13 digits"
    fun errNtnPfa(ctx: Context) = if (isUrdu(ctx)) "NTN aur PFA license likho" else "Enter NTN and PFA license"

    fun entryGetStarted(ctx: Context) = if (isUrdu(ctx)) "Shuru karo" else "Get Started"
    fun entrySignInLink(ctx: Context) = if (isUrdu(ctx)) "Pehle se account hai? Login" else "Already have account? Sign in"
    fun entryTagline(ctx: Context) = if (isUrdu(ctx)) "Apna restaurant HUBB par list karo. Orders lo. Paise kamao." else "List your restaurant on HUBB. Receive orders. Grow your business."

    fun tabDashboard(ctx: Context) = if (isUrdu(ctx)) "Home" else "Dashboard"
    fun tabOrders(ctx: Context) = "Orders"
    fun tabMenu(ctx: Context) = "Menu"
    fun tabPromotion(ctx: Context) = if (isUrdu(ctx)) "Offers" else "Promotion"
    fun tabMore(ctx: Context) = if (isUrdu(ctx)) "Aur" else "More"
    fun tabChain(ctx: Context) = "Chain"
    fun tabPayouts(ctx: Context) = if (isUrdu(ctx)) "Payout" else "Payouts"
    fun tabSettings(ctx: Context) = if (isUrdu(ctx)) "Settings" else "Settings"

    fun menuLoading(ctx: Context) = if (isUrdu(ctx)) "Load ho raha hai…" else "Loading…"
    fun menuUnavailableTitle(ctx: Context) = if (isUrdu(ctx)) "Menu mojood nahi" else "Menu not available"
    fun menuUnavailableSubtitle(ctx: Context) = if (isUrdu(ctx)) "Menu manage karne ke liye onboarding mukammal karein." else "Complete onboarding to manage your menu."
    fun menuPendingTitle(ctx: Context) = if (isUrdu(ctx)) "Menu ki manzoori intezar mein" else "Menu setup pending approval"
    fun menuPendingSubtitle(ctx: Context) = if (isUrdu(ctx)) "Naye venue ki review chal rahi hai. Jab operations approve karega, yahan menu add kar sakte hain." else "Your new venue is being reviewed. Once operations approves registration, you can add and edit your menu here."
    fun profileLoadError(ctx: Context) = if (isUrdu(ctx)) "Profile load nahi ho saki" else "Could not load profile"
    fun salesOverview(ctx: Context) = if (isUrdu(ctx)) "Sales" else "Sales overview"
    fun todaysGoal(ctx: Context) = if (isUrdu(ctx)) "Aaj ka goal" else "Today's goal"
    fun walletBalance(ctx: Context) = if (isUrdu(ctx)) "Wallet" else "Wallet Balance"
    fun availablePayout(ctx: Context) = if (isUrdu(ctx)) "Payout ke liye" else "Available for payout"
    fun partnerTipsTitle(ctx: Context) = if (isUrdu(ctx)) "Pakistan partners" else "Tips for Pakistan"
    fun partnerTipsSubtitle(ctx: Context) = if (isUrdu(ctx)) "Cash on delivery, mobile wallet, aur support" else "COD, JazzCash / Easypaisa, and local support"
    fun partnerTipCod(ctx: Context) = if (isUrdu(ctx)) "Ziyada tar orders cash on delivery hote hain — rider ko exact change rakhna asaan karein." else "Most deliveries are cash on delivery — keep change ready and seal bags for riders."
    fun partnerTipWallet(ctx: Context) = if (isUrdu(ctx)) "Payout JazzCash ya Easypaisa par set karein (Wallet section) — bank IBAN bhi chalega." else "Set JazzCash or Easypaisa in Wallet & payouts (ops portal) — bank IBAN works too."
    fun partnerTipMenu(ctx: Context) = if (isUrdu(ctx)) "Catalog photo se AI import ke baad team publish karti hai — customers ko purana live catalog dikhta rahega." else "After catalog photo import, ops publishes — customers keep seeing your last live catalog until then."
    fun partnerTipPeak(ctx: Context) = if (isUrdu(ctx)) "Iftar / peak hours par prep time barha dein taake late orders na hon." else "Raise prep time during peak & Iftar hours to protect ratings."
    fun chatWhatsApp(ctx: Context) = if (isUrdu(ctx)) "WhatsApp support" else "Chat on WhatsApp"
    fun languageSettings(ctx: Context) = if (isUrdu(ctx)) "Zaban" else "Language"
    fun languageEnglish(ctx: Context) = "English"
    fun languageRomanUrdu(ctx: Context) = "Roman Urdu"

    fun submitForReviewBannerTitle(ctx: Context) = if (isUrdu(ctx)) "Team se catalog check karwao" else "Request catalog review"
    fun submitForReview(ctx: Context) = if (isUrdu(ctx)) "Review ke liye bhejo" else "Submit for review"
    fun submitForReviewHint(ctx: Context) = if (isUrdu(ctx)) "HUBB team approve karne ke baad customers ko catalog dikhega." else "HUBB ops will review and publish — then customers see this catalog on the app."

    // HUBB Merchant catalog (restaurants & shops) — parity with iOS ResL10n
    fun hubbCatalogNavTitle(ctx: Context) = if (isUrdu(ctx)) "HUBB catalog" else "HUBB Catalog"
    fun menuLoadingTitle(ctx: Context) = if (isUrdu(ctx)) "Catalog load ho raha hai…" else "Loading catalog…"
    fun menuLoadingSubtitle(ctx: Context) = if (isUrdu(ctx)) "Categories aur items aa rahe hain." else "Fetching your categories and items."
    fun catalogEmptyTitle(ctx: Context) = if (isUrdu(ctx)) "Abhi catalog khali hai" else "No catalog yet"
    fun catalogEmptyBody(ctx: Context) = if (isUrdu(ctx)) {
        "Printed menu ya product list ki saaf photo bhejein — HUBB AI sections, naam aur PKR prices nikalta hai. Restaurants aur shops dono ke liye."
    } else {
        "Add a clear photo of your printed menu or product list — HUBB uses AI to extract sections, names, and PKR prices. Works for restaurants and shops."
    }
    fun scanMenuPhoto(ctx: Context) = if (isUrdu(ctx)) "Photo se scan karein" else "Scan with photo"
    fun scanMenuAI(ctx: Context) = if (isUrdu(ctx)) "AI se catalog scan" else "Scan catalog with AI"
    fun addCategoryToolbar(ctx: Context) = if (isUrdu(ctx)) "Category shamil karein" else "Add category"
    fun addCategoryDialogTitle(ctx: Context) = addCategoryToolbar(ctx)
    fun categoryNamePlaceholder(ctx: Context) = if (isUrdu(ctx)) "Category ka naam" else "Category name"
    fun startEmptyCategory(ctx: Context) = if (isUrdu(ctx)) "Khali category se shuru karein" else "Start with an empty category"
    fun importNoteTitle(ctx: Context) = if (isUrdu(ctx)) "Import note" else "Import note"
    fun aiCatalogScanTitle(ctx: Context) = if (isUrdu(ctx)) "HUBB AI catalog scan" else "HUBB AI catalog scan"
    fun aiCatalogScanBody(ctx: Context) = if (isUrdu(ctx)) {
        "Photo aap ke phone par resize hoti hai, phir secure bheji jati hai. Servers AI vision se dishes / products aur prices read karte hain."
    } else {
        "Photos are resized on your phone, then sent securely. HUBB’s servers read dishes or products and PKR prices with AI vision."
    }
    fun readingCatalog(ctx: Context) = if (isUrdu(ctx)) "Catalog parh rahe hain…" else "Reading your catalog…"
    fun readingCatalogWait(ctx: Context) = if (isUrdu(ctx)) {
        "AI items aur prices nikaal raha hai — aksar 20–60 second."
    } else {
        "AI is extracting items and prices — usually 20–60 seconds."
    }
    fun changesRequestedTitle(ctx: Context) = if (isUrdu(ctx)) "Changes maangi gayi" else "Changes requested"
    fun changesRequestedBody(ctx: Context) = if (isUrdu(ctx)) {
        "Neeche items update karein, phir review ke liye dobara bhejein."
    } else {
        "Update items below, then resubmit for HUBB review."
    }
    fun resubmitForReview(ctx: Context) = if (isUrdu(ctx)) "Dobara review ke liye bhejein" else "Resubmit for review"
    fun hubbLiveCatalogLine(ctx: Context, categories: Int, items: Int) = if (isUrdu(ctx)) {
        "Live ab: $categories categories, $items items"
    } else {
        "Live on HUBB now: $categories categories, $items items"
    }
    fun importTipLighting(ctx: Context) = if (isUrdu(ctx)) "Achhi roshni — zyada glare se bachein." else "Good lighting — avoid heavy glare."
    fun importTipFraming(ctx: Context) = if (isUrdu(ctx)) "Poora page frame mein — text saaf readable ho." else "Fit the whole page; text must be readable."
    fun importTipNetwork(ctx: Context) = if (isUrdu(ctx)) "Wi‑Fi behtar — processing minute tak le sakti hai." else "Use Wi‑Fi — processing can take up to a minute."
    fun chooseFromLibrary(ctx: Context) = if (isUrdu(ctx)) "Library se chunein" else "Choose from library"
    fun takePhoto(ctx: Context) = if (isUrdu(ctx)) "Photo lein" else "Take a photo"
    fun addNewCategory(ctx: Context) = if (isUrdu(ctx)) "Nayi category" else "Add new category"
    fun addItemToCategory(ctx: Context, categoryName: String) = if (isUrdu(ctx)) {
        "$categoryName mein item jodein"
    } else {
        "Add item to $categoryName"
    }
    fun itemDetailsSection(ctx: Context) = if (isUrdu(ctx)) "Tafseel" else "Item details"
    fun addToCategoryTitle(ctx: Context, categoryName: String) = if (isUrdu(ctx)) {
        "$categoryName mein jodein"
    } else {
        "Add to $categoryName"
    }
    fun editItemTitle(ctx: Context) = if (isUrdu(ctx)) "Item edit" else "Edit item"
    fun itemNameLabel(ctx: Context) = if (isUrdu(ctx)) "Item ka naam" else "Item name"
    @Suppress("UNUSED_PARAMETER")
    fun priceRsLabel(ctx: Context) = "Price (Rs.)"
    fun descriptionOptionalLabel(ctx: Context) = if (isUrdu(ctx)) "Tafseel (optional)" else "Description (optional)"
    fun descriptionLabel(ctx: Context) = if (isUrdu(ctx)) "Tafseel" else "Description"
    fun availableLabel(ctx: Context) = if (isUrdu(ctx)) "Mojood / order ke liye" else "Available for order"
    fun dismiss(ctx: Context) = if (isUrdu(ctx)) "Band karein" else "Dismiss"
    fun noteLabel(ctx: Context, note: String) = if (isUrdu(ctx)) "Note: $note" else "Note: $note"

    // More hub (parity with iOS)
    fun moreHubBrandSubtitle(ctx: Context) = if (isUrdu(ctx)) "HUBB partner tools" else "HUBB Merchant tools"
    fun moreSectionTools(ctx: Context) = if (isUrdu(ctx)) "Business tools" else "Business tools"
    fun moreRowChainTitle(ctx: Context) = if (isUrdu(ctx)) "Chain overview" else "Chain overview"
    fun moreRowChainSubtitle(ctx: Context) = if (isUrdu(ctx)) "Branches aur metrics" else "Branches & chain metrics"
    fun moreRowWalletTitle(ctx: Context) = if (isUrdu(ctx)) "Wallet & payouts" else "Wallet & payouts"
    fun moreRowWalletSubtitle(ctx: Context) = if (isUrdu(ctx)) "Balance, earnings, payouts" else "Balance, earnings, payouts"
    fun moreRowReviewsTitle(ctx: Context) = "Reviews"
    fun moreRowReviewsSubtitle(ctx: Context) = if (isUrdu(ctx)) "Customer feedback" else "Customer feedback"
    fun moreRowIssuesTitle(ctx: Context) = if (isUrdu(ctx)) "Order issues" else "Order issues"
    fun moreRowIssuesSubtitle(ctx: Context) = if (isUrdu(ctx)) "Refunds & disputes" else "Refunds & disputes"
    fun moreRowInboxTitle(ctx: Context) = "Inbox"
    fun moreRowInboxSubtitle(ctx: Context) = if (isUrdu(ctx)) "Alerts & messages" else "Alerts & messages"
    fun moreRowSettingsTitle(ctx: Context) = if (isUrdu(ctx)) "Settings" else "Settings"
    @Suppress("UNUSED_PARAMETER")
    fun moreRowSettingsSubtitle(ctx: Context) = "Restaurant status, notifications, account"
    fun navChainOverview(ctx: Context) = moreRowChainTitle(ctx)
    fun navWalletPayouts(ctx: Context) = moreRowWalletTitle(ctx)
    fun navReviews(ctx: Context) = "Reviews"
    fun navOrderIssues(ctx: Context) = moreRowIssuesTitle(ctx)
    fun navInbox(ctx: Context) = "Inbox"

    fun moreHubPakistanTipTitle(ctx: Context) = if (isUrdu(ctx)) "Pakistan partners" else "Pakistan partners"
    fun moreHubPakistanTipBody(ctx: Context) = if (isUrdu(ctx)) {
        "COD zyada common hai — rider ke liye change aur sealed bags. Iftar / peak par prep time barhaein."
    } else {
        "COD is common — keep change ready and seal bags. Raise prep time during Iftar and peak hours."
    }

    // Home Kitchen
    fun hkTitle(ctx: Context) = if (isUrdu(ctx)) "Ghar ka Kitchen" else "Home Kitchen"
    fun hkWelcome(ctx: Context) = if (isUrdu(ctx)) "Ghar se khana becho!" else "Sell food from home!"
    fun hkSubtitle(ctx: Context) = if (isUrdu(ctx)) "Bohat asaan hai \u2014 sirf 5 minute mein shuru karein" else "It's super easy \u2014 start in just 5 minutes"
    fun hkEntryButton(ctx: Context) = if (isUrdu(ctx)) "Ghar se Khana Becho" else "Start Home Kitchen"
    fun hkVerified(ctx: Context) = if (isUrdu(ctx)) "Tasdeeq Shuda Kitchen" else "Verified Kitchen"
    fun hkStarter(ctx: Context) = if (isUrdu(ctx)) "Ghar ka Kitchen" else "Home Kitchen"
    fun hkMyKitchen(ctx: Context) = if (isUrdu(ctx)) "Mera Kitchen" else "My Kitchen"
    fun hkUpgrade(ctx: Context) = if (isUrdu(ctx)) "Level 2 mein upgrade karein" else "Upgrade to Level 2"
}
