package com.cibus.restaurant.claim

/**
 * RestaurantClaimModels.kt
 * Restaurant Claiming + Verification System
 *
 * Defines ownership and claim state for restaurant listings.
 * Operational access is ONLY granted at VERIFIED_PARTNER.
 */

// ── Listing State ─────────────────────────────────────────────────────────────

enum class RestaurantListingState(val raw: String, val displayLabel: String) {
    IMPORTED_PUBLIC   ("imported_public",   "Public Listing"),
    UNCLAIMED         ("unclaimed",         "Unclaimed"),
    CLAIM_SUBMITTED   ("claim_submitted",   "Claim Submitted"),
    UNDER_REVIEW      ("under_review",      "Under Review"),
    NEEDS_MORE_INFO   ("needs_more_info",   "More Info Required"),
    VERIFIED_PARTNER  ("verified_partner",  "Verified Partner"),
    REJECTED          ("rejected",          "Rejected"),
    SUSPENDED         ("suspended",         "Suspended");

    val isOperational: Boolean get() = this == VERIFIED_PARTNER

    companion object {
        fun from(raw: String?): RestaurantListingState =
            values().firstOrNull { it.raw == raw } ?: UNCLAIMED
    }
}

// ── Claimant Role ─────────────────────────────────────────────────────────────

enum class ClaimantRole(val raw: String, val label: String) {
    OWNER    ("owner",    "Owner"),
    MANAGER  ("manager",  "Manager"),
    OPERATOR ("operator", "Operator");

    companion object {
        val all = values().toList()
    }
}

// ── Claim Document Type ───────────────────────────────────────────────────────

enum class ClaimDocumentType(
    val raw: String,
    val displayLabel: String,
    val isRequired: Boolean
) {
    OWNER_CNIC            ("owner_cnic",             "Owner CNIC",              true),
    BUSINESS_REGISTRATION ("business_registration",  "Business Registration",   false),
    UTILITY_BILL          ("utility_bill",           "Utility Bill",            false),
    TENANCY_PROOF         ("tenancy_proof",          "Tenancy / Shop Proof",    true),
    RESTAURANT_LETTERHEAD ("restaurant_letterhead",  "Restaurant Letterhead",   false),
    BANK_STATEMENT        ("bank_statement",         "Bank Statement",          false),
    OTHER                 ("other",                  "Other Document",          false);

    companion object {
        val all = values().toList()
    }
}

// ── Claim Document ────────────────────────────────────────────────────────────

data class ClaimDocument(
    val id: String,
    val type: ClaimDocumentType,
    val fileName: String? = null,
    val uploadUrl: String? = null,
    val isPendingUpload: Boolean = false
)

// ── Payout Info ───────────────────────────────────────────────────────────────

data class RestaurantPayoutInfo(
    val accountTitle: String = "",
    val bankName: String = "",
    val iban: String = "",
    val accountNumber: String? = null,
    val jazzCashWallet: String? = null,
    val easypaisaWallet: String? = null
)

// ── Claim Request ─────────────────────────────────────────────────────────────

data class RestaurantClaimRequest(
    val restaurantId: String,
    val restaurantName: String,
    val ownerName: String,
    val role: String,              // ClaimantRole.raw
    val email: String,
    val phone: String,
    val cnic: String,
    val businessName: String?,
    val ntnNumber: String?,
    val pfaLicenseNumber: String?,
    val notes: String?,
    val confirmedAddress: String,
    val payoutInfo: RestaurantPayoutInfo?,
    val documents: List<ClaimDocument> = emptyList()
)

// ── Claim Status Summary ──────────────────────────────────────────────────────

data class ClaimStatusSummary(
    val state: RestaurantListingState,
    val claimId: String?,
    val reviewNote: String?
) {
    val canOperate: Boolean get() = state.isOperational
    val isWaiting: Boolean get() = state == RestaurantListingState.CLAIM_SUBMITTED ||
                                    state == RestaurantListingState.UNDER_REVIEW
    val needsAction: Boolean get() = state == RestaurantListingState.NEEDS_MORE_INFO
}
