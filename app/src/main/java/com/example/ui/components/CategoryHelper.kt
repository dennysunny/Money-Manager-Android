package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BabyChangingStation
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildFriendly
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

object CategoryHelper {
    val iconOptions = listOf(
        CategoryIconOption("gas", "Fuel", Icons.Default.LocalGasStation),
        CategoryIconOption("food", "Food", Icons.Default.Fastfood),
        CategoryIconOption("grocery", "Grocery", Icons.Default.ShoppingBag),
        CategoryIconOption("home", "Home", Icons.Default.Home),
        CategoryIconOption("car", "Vehicle", Icons.Default.DirectionsCar),
        CategoryIconOption("medical", "Health", Icons.Default.LocalHospital),
        CategoryIconOption("cloth", "Clothes", Icons.Default.Checkroom),
        CategoryIconOption("gym", "Fitness", Icons.Default.FitnessCenter),
        CategoryIconOption("bills", "Bills", Icons.Default.Receipt),
        CategoryIconOption("phone", "Mobile", Icons.Default.Smartphone),
        CategoryIconOption("travel", "Travel", Icons.Default.Flight),
        CategoryIconOption("movie", "Cinema", Icons.Default.Movie),
        CategoryIconOption("sports", "Sports", Icons.Default.SportsSoccer),
        CategoryIconOption("school", "School", Icons.Default.School),
        CategoryIconOption("baby", "Baby", Icons.Default.ChildFriendly),
        CategoryIconOption("salary", "Income", Icons.Default.Payments),
        CategoryIconOption("bank", "Bank", Icons.Default.AccountBalance),
        CategoryIconOption("card", "Card", Icons.Default.CreditCard),
        CategoryIconOption("work", "Work", Icons.Default.Work),
        CategoryIconOption("star", "Star", Icons.Default.Star)
    )

    val commonExpenseCategories = listOf(
        "Petrol",
        "Grocery Home",
        "Food",
        "Snacks",
        "Maternity",
        "EMI",
        "Home Expenses",
        "Flat Expenses",
        "Medical",
        "Clothing",
        "Gym",
        "Health",
        "Bills",
        "Car / Bike Expenses",
        "Mobile Recharge",
        "Tech",
        "Baby",
        "Gifts",
        "Transportation",
        "Entertainment",
        "Shopping",
        "Wedding Expenses",
        "Others"
    )

    val commonIncomeCategories = listOf(
        "Salary",
        "Debt",
        "Allowance",
        "Bill Reimbursement",
        "Purchase",
        "Loan",
        "Bonus",
        "Investment",
        "Other Income"
    )

    fun getCategoryIcon(category: String, iconKey: String = ""): ImageVector {
        if (iconKey.isNotBlank()) {
            val matched = iconOptions.firstOrNull { it.key.equals(iconKey, ignoreCase = true) }
            if (matched != null) return matched.icon
        }

        val cat = category.lowercase()
        return when {
            cat.contains("petrol") || cat.contains("fuel") || cat.contains("gas") -> Icons.Default.LocalGasStation
            cat.contains("grocery") -> Icons.Default.ShoppingBag
            cat.contains("food") || cat.contains("lunch") || cat.contains("dinner") -> Icons.Default.Fastfood
            cat.contains("snack") || cat.contains("tea") || cat.contains("coffee") -> Icons.Default.Fastfood
            cat.contains("maternity") || cat.contains("baby") -> Icons.Default.ChildFriendly
            cat.contains("emi") || cat.contains("loan") -> Icons.Default.CreditCard
            cat.contains("home") || cat.contains("flat") || cat.contains("rent") -> Icons.Default.Home
            cat.contains("medical") || cat.contains("hospital") || cat.contains("medicine") -> Icons.Default.LocalHospital
            cat.contains("cloth") || cat.contains("shirt") || cat.contains("pant") -> Icons.Default.Checkroom
            cat.contains("gym") || cat.contains("fitness") -> Icons.Default.FitnessCenter
            cat.contains("health") -> Icons.Default.LocalHospital
            cat.contains("bill") || cat.contains("electric") || cat.contains("tax") -> Icons.Default.Receipt
            cat.contains("car") || cat.contains("bike") -> Icons.Default.DirectionsCar
            cat.contains("recharge") || cat.contains("mobile") || cat.contains("phone") -> Icons.Default.Smartphone
            cat.contains("tech") || cat.contains("computer") -> Icons.Default.Smartphone
            cat.contains("transport") || cat.contains("metro") || cat.contains("bus") || cat.contains("uber") -> Icons.Default.DirectionsCar
            cat.contains("entertain") || cat.contains("movie") || cat.contains("netflix") -> Icons.Default.Movie
            cat.contains("travel") || cat.contains("trip") || cat.contains("hotel") -> Icons.Default.Flight
            cat.contains("sport") || cat.contains("turf") -> Icons.Default.SportsSoccer
            cat.contains("education") -> Icons.Default.School
            cat.contains("salary") -> Icons.Default.Payments
            cat.contains("debt") -> Icons.Default.AccountBalance
            else -> Icons.Default.Payments
        }
    }

    fun getCategoryColor(category: String): Color {
        val cat = category.lowercase()
        return when {
            cat.contains("petrol") || cat.contains("gas") -> Color(0xFFF97316) // Orange
            cat.contains("grocery") -> Color(0xFF10B981) // Green
            cat.contains("food") -> Color(0xFFEAB308) // Yellow
            cat.contains("snack") -> Color(0xFFF59E0B) // Amber
            cat.contains("maternity") || cat.contains("baby") -> Color(0xFFEC4899) // Pink
            cat.contains("emi") -> Color(0xFF8B5CF6) // Purple
            cat.contains("home") || cat.contains("flat") -> Color(0xFF3B82F6) // Blue
            cat.contains("medical") || cat.contains("health") -> Color(0xFFEF4444) // Red
            cat.contains("cloth") -> Color(0xFF06B6D4) // Cyan
            cat.contains("gym") -> Color(0xFF6366F1) // Indigo
            cat.contains("bills") -> Color(0xFF64748B) // Slate
            cat.contains("car") || cat.contains("bike") -> Color(0xFF0284C7)
            cat.contains("recharge") || cat.contains("tech") -> Color(0xFF8B5CF6)
            cat.contains("salary") -> Color(0xFF10B981)
            cat.contains("debt") -> Color(0xFFF43F5E)
            else -> Color(0xFF6B7280)
        }
    }
}
