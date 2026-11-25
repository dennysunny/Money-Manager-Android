package com.example.receiver

import java.util.regex.Pattern

data class ParsedSmsTransaction(
    val amount: Double,
    val type: String, // "Expense" or "Income"
    val category: String,
    val memo: String,
    val wallet: String,
    val rawSnippet: String
)

object SmsTransactionParser {

    // Regex patterns for currency amounts (e.g. INR 450, Rs. 1,299.00, Rs 500, ₹ 350.50, $45.00)
    private val currencyRegex = Pattern.compile(
        """(?i)(?:inr|rs\.?|₹|\$)\s*([0-9]+(?:,[0-9]+)*(?:\.[0-9]{1,2})?)"""
    )

    private val amountSuffixRegex = Pattern.compile(
        """(?i)\b([0-9]+(?:,[0-9]+)*(?:\.[0-9]{1,2})?)\s*(?:inr|rs\.?|₹)"""
    )

    // Keywords that indicate financial debit/expense
    private val expenseKeywords = listOf(
        "debited", "spent", "paid", "withdrawn", "payment of", "purchase of",
        "charged", "sent to", "transferred to", "vpa", "txn of", "deducted"
    )

    // Keywords that indicate financial credit/income
    private val incomeKeywords = listOf(
        "credited", "received", "refund of", "cashback", "deposited",
        "added to your account", "salary credited", "funds received"
    )

    // Negative filter keywords (ignore pure OTP messages that aren't completed transactions)
    private val nonTransactionKeywords = listOf(
        "otp for login", "login otp", "verification code", "security code",
        "do not share your password", "secret otp"
    )

    fun parse(sender: String?, body: String?): ParsedSmsTransaction? {
        if (body.isNullOrBlank()) return null

        val cleanBody = body.trim()
        val lowerBody = cleanBody.lowercase()

        // 1. Filter out pure login OTPs
        if (nonTransactionKeywords.any { lowerBody.contains(it) } &&
            !expenseKeywords.any { lowerBody.contains(it) } &&
            !incomeKeywords.any { lowerBody.contains(it) }
        ) {
            return null
        }

        // 2. Identify if it is Debit/Expense or Credit/Income
        val hasDebit = expenseKeywords.any { lowerBody.contains(it) }
        val hasCredit = incomeKeywords.any { lowerBody.contains(it) }

        if (!hasDebit && !hasCredit) {
            // Not a recognized transaction message
            return null
        }

        val type = if (hasDebit) "Expense" else "Income"

        // 3. Extract the Transaction Amount
        var amount: Double? = null

        val currMatcher = currencyRegex.matcher(cleanBody)
        if (currMatcher.find()) {
            val amtStr = currMatcher.group(1)?.replace(",", "")
            amount = amtStr?.toDoubleOrNull()
        }

        if (amount == null) {
            val suffixMatcher = amountSuffixRegex.matcher(cleanBody)
            if (suffixMatcher.find()) {
                val amtStr = suffixMatcher.group(1)?.replace(",", "")
                amount = amtStr?.toDoubleOrNull()
            }
        }

        if (amount == null || amount <= 0.0) {
            return null
        }

        // 4. Extract Merchant / Payee / Reference
        val memo = extractMerchant(cleanBody, sender)

        // 5. Guess Category based on keywords in body or merchant
        val category = guessCategory(lowerBody, type)

        // 6. Detect Wallet (Credit Card vs Cash / UPI)
        val wallet = if (
            lowerBody.contains("credit card") ||
            lowerBody.contains("card ending") ||
            lowerBody.contains("card xx") ||
            lowerBody.contains("card no")
        ) {
            "Credit Card"
        } else {
            "Cash / UPI"
        }

        val snippet = if (cleanBody.length > 90) cleanBody.take(87) + "..." else cleanBody

        return ParsedSmsTransaction(
            amount = amount,
            type = type,
            category = category,
            memo = memo,
            wallet = wallet,
            rawSnippet = snippet
        )
    }

    private fun extractMerchant(body: String, sender: String?): String {
        // Try to match "at <Merchant>", "to <Merchant>", "vpa <Merchant>", "info: <Merchant>"
        val merchantPatterns = listOf(
            """(?i)\bat\s+([A-Za-z0-9\s\.\-_&]{2,25}?)(?=\s+(?:on|using|via|ref|bal|available|\.|\,))""",
            """(?i)\bto\s+([A-Za-z0-9\s\.\-_&]{2,25}?)(?=\s+(?:on|using|via|ref|bal|available|\.|\,))""",
            """(?i)\bvpa\s+([A-Za-z0-9\.\-_@]{3,30})""",
            """(?i)\binfo\s*:\s*([A-Za-z0-9\s\.\-_]{2,25}?)"""
        )

        for (regex in merchantPatterns) {
            val matcher = Pattern.compile(regex).matcher(body)
            if (matcher.find()) {
                val matched = matcher.group(1)?.trim()
                if (!matched.isNullOrBlank() && matched.length > 1) {
                    return cleanMerchantName(matched)
                }
            }
        }

        // Fallback: use sender ID cleaned (e.g. "HDFCBK" -> "HDFC Bank", "SBIINB" -> "SBI")
        return sender?.let { cleanSenderName(it) } ?: "Payment"
    }

    private fun cleanMerchantName(name: String): String {
        return name
            .replace(Regex("""(?i)\b(?:vpa|upi|ref|txn|a/c|acct|ac)\b"""), "")
            .trim()
            .take(28)
            .ifBlank { "Payment" }
    }

    private fun cleanSenderName(sender: String): String {
        val s = sender.uppercase()
        return when {
            s.contains("HDFC") -> "HDFC Bank"
            s.contains("SBI") -> "State Bank of India"
            s.contains("ICICI") -> "ICICI Bank"
            s.contains("AXIS") -> "Axis Bank"
            s.contains("KOTAK") -> "Kotak Bank"
            s.contains("PAYTM") -> "Paytm"
            s.contains("GPAY") -> "Google Pay"
            s.contains("PHONEPE") -> "PhonePe"
            s.contains("AMAZON") -> "Amazon Pay"
            s.contains("PNB") -> "Punjab National Bank"
            s.contains("BOB") -> "Bank of Baroda"
            else -> sender.takeLast(6)
        }
    }

    private fun guessCategory(lower: String, type: String): String {
        if (type == "Income") {
            return when {
                lower.contains("salary") || lower.contains("payroll") -> "Salary"
                lower.contains("cashback") || lower.contains("reward") -> "Bonus"
                lower.contains("interest") || lower.contains("dividend") -> "Investments"
                lower.contains("freelance") || lower.contains("consult") -> "Freelance"
                else -> "Income"
            }
        }

        return when {
            // Food
            lower.contains("swiggy") || lower.contains("zomato") || lower.contains("restaurant") ||
            lower.contains("cafe") || lower.contains("mcdonald") || lower.contains("dominos") ||
            lower.contains("burger") || lower.contains("starbucks") || lower.contains("kfc") ||
            lower.contains("eatclub") || lower.contains("pizza") || lower.contains("bakery") ||
            lower.contains("dine") -> "Food"

            // Petrol / Fuel
            lower.contains("petrol") || lower.contains("fuel") || lower.contains("shell") ||
            lower.contains("hpcl") || lower.contains("bpcl") || lower.contains("iocl") ||
            lower.contains("indian oil") || lower.contains("cng") || lower.contains("gas station") -> "Petrol"

            // Grocery
            lower.contains("blinkit") || lower.contains("zepto") || lower.contains("instamart") ||
            lower.contains("bigbasket") || lower.contains("dmart") || lower.contains("supermarket") ||
            lower.contains("grocery") || lower.contains("mart") -> "Groceries"

            // Shopping
            lower.contains("amazon") || lower.contains("flipkart") || lower.contains("myntra") ||
            lower.contains("ajio") || lower.contains("meesho") || lower.contains("zara") ||
            lower.contains("h&m") || lower.contains("retail") || lower.contains("clothing") ||
            lower.contains("shopping") -> "Shopping"

            // Transport / Travel
            lower.contains("uber") || lower.contains("ola") || lower.contains("rapido") ||
            lower.contains("metro") || lower.contains("irctc") || lower.contains("railway") ||
            lower.contains("indigo") || lower.contains("flight") || lower.contains("redbus") ||
            lower.contains("toll") || lower.contains("fastag") -> "Transport"

            // Bills & Utilities
            lower.contains("electricity") || lower.contains("bescom") || lower.contains("water") ||
            lower.contains("airtel") || lower.contains("jio") || lower.contains("vodafone") ||
            lower.contains("broadband") || lower.contains("recharge") || lower.contains("billdesk") ||
            lower.contains("utility") || lower.contains("gas bill") -> "Bills"

            // Entertainment
            lower.contains("netflix") || lower.contains("spotify") || lower.contains("hotstar") ||
            lower.contains("prime") || lower.contains("pvr") || lower.contains("inox") ||
            lower.contains("movie") || lower.contains("bookmyshow") || lower.contains("youtube") -> "Entertainment"

            // Healthcare
            lower.contains("apollo") || lower.contains("pharmacy") || lower.contains("1mg") ||
            lower.contains("hospital") || lower.contains("clinic") || lower.contains("pharmeasy") ||
            lower.contains("medicine") || lower.contains("dental") || lower.contains("medplus") -> "Healthcare"

            // EMI / Loan
            lower.contains("emi") || lower.contains("loan") || lower.contains("bajaj") -> "EMI"

            else -> "Others"
        }
    }
}
