package com.zimneos.scan2contact.core

import com.google.mlkit.vision.text.Text
import com.zimneos.scan2contact.ui.screens.BusinessCardDetails
import java.util.Locale

fun extractBusinessCardDetails(
    visionText: Text,
    allTextSentences: (MutableList<String>) -> Unit
): BusinessCardDetails {
    var details = BusinessCardDetails()
    var isBusinessCardCount = 0
    val sentences = mutableListOf<String>()
    val phoneRegex = Regex("^\\+?\\d{7,15}$")
    val websiteRegex = Regex("^(https?://|www\\.)[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}$")
    val keywordRegex = Regex(
        "\\b(Phone|Mobile|Contact|Email|Website|Company|Address|Fax|CEO|Manager|Founder|CTO|Director|VP|Consultant|Designation|Title|Job Title)\\b",
        RegexOption.IGNORE_CASE
    )
    val designationTerms = listOf(
        "President",
        "Vice President",
        "VP",
        "Manager",
        "Developer",
        "Analyst",
        "Consultant",
        "Specialist",
        "Coordinator",
        "Executive",
        "Officer",
        "Associate",
        "Representative",
        "Clerk",
        "Technician",
        "Supervisor",
        "Agent",
        "Architect",
        "Designer",
        "Planner",
        "Accountant",
        "Auditor",
        "Librarian",
        "Pharmacist",
        "Therapist",
        "Instructor",
        "Professor",
        "Lecturer",
        "Teacher",
        "Tutor",
        "Counselor",
        "Advisor",
        "Coach",
        "Trainer",
        "Scientist",
        "Researcher",
        "Investigator",
        "Journalist",
        "Editor",
        "Writer",
        "Author",
        "Artist",
        "Musician",
        "Composer",
        "Performer",
        "Producer",
        "Director",
        "Engineer",
        "Analyst",
        "Consultant",
        "Specialist",
        "Coordinator",
        "Executive",
        "Officer",
        "Associate",
        "Representative",
        "Clerk",
        "Technician",
        "Supervisor",
        "Agent",
        "Architect",
        "Designer",
        "Planner",
        "Accountant",
        "Auditor",
        "Pharmacist",
        "Therapist",
        "Instructor",
        "Professor",
        "Lecturer",
        "Teacher",
        "Tutor",
        "Counselor",
        "Advisor",
        "Coach",
        "Trainer",
        "Intern",
        "Trainee",
        "Volunteer",
        "Assistant",
        "Secretary",
        "Receptionist",
        "Admin",
        "Administrator",
        "HR",
        "Marketing",
        "Sales",
        "Finance",
        "Operations",
        "Technology",
        "IT",
        "Legal",
        "Compliance",
        "Risk",
        "Audit",
        "Support",
        "Customer Service",
        "PR",
        "Communications",
        "Business Development",
        "BD",
        "R&D",
        "Research and Development",
        "General Manager",
        "GM",
        "Project Manager",
        "PM",
        "Product Manager",
        "Chief",
        "Chief Business",
        "Chief Business Development Officer",
        "Chief Marketing",
        "Chief Operations",
        "Chief Technology",
        "Chief Executive",
        "Lead",
        "Chief Executive Officer",
        "CEO",
        "Chief Technology Officer",
        "CTO",
        "Chief Financial Officer",
        "CFO",
        "Chief Marketing Officer",
        "CMO",
        "Chief Operating Officer",
        "COO",
        "Chief Information Officer",
        "CIO",
        "Founder",
        "Co-Founder",
        "Owner",
        "Partner",
        "Principal"
    )
    val addressRegex = Regex(
        "\\d{0,5}\\s?[\\w\\s\\-.]+\\s?(?:Street|St|Avenue|Door|Ave|Boulevard|Blvd|Road|Rd|Lane|Ln|Drive|Dr|Court|Ct|Square|Sq|Building|Suite|Floor|Apt|Apartment|Box|P\\.O\\. Box|Post Office Box|PO BOX|PW)\\b.*$",
        RegexOption.IGNORE_CASE
    )
    val postalCodeRegex = Regex("\\b\\d{5}(-\\d{4})?\\b")
    val cityStateRegex = Regex("\\b[A-Z][a-z]+,\\s?[A-Z]{2}\\b")
    val companyKeywordsRegex =
        Regex("\\b(Company|Organization|Corp|Inc|Ltd|GmbH|Pvt\\.? LTD)\\b", RegexOption.IGNORE_CASE)
    val indianStates = listOf(
        "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa",
        "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala",
        "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland",
        "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
        "Uttar Pradesh", "Uttarakhand", "West Bengal"
    )
    val countryNames = listOf(
        "india", "usa", "united states", "uk", "netherlands", "canada", "australia",
        "china", "japan", "france", "germany", "italy", "spain", "brazil", "mexico",
        "russia", "south africa", "singapore", "malaysia", "thailand", "indonesia",
        "philippines", "vietnam", "pakistan", "bangladesh", "egypt", "nigeria", "kenya",
        "south korea"
    )
    val popularIndianCities = listOf(
        "Mumbai",
        "Delhi",
        "Bangalore",
        "Hyderabad",
        "Ahmedabad",
        "Chennai",
        "Kolkata",
        "Surat",
        "Pune",
        "Jaipur",
        "Lucknow",
        "Kanpur",
        "Nagpur",
        "Indore",
        "Bhopal",
        "noida",
        "Vadodara",
        "Ghaziabad",
        "Ludhiana",
        "Coimbatore",
        "Agra",
        "Visakhapatnam",
        "Kochi",
        "Madurai",
        "Varanasi",
        "Meerut",
        "Faridabad",
        "Rajkot",
        "Jamshedpur",
        "Srinagar",
        "Jabalpur",
        "Asansol",
        "Vasai-Virar City",
        "Allahabad",
        "Dhanbad",
        "Aurangabad",
        "Amritsar",
        "Jodhpur",
        "Ranchi",
        "Raipur",
        "Guwahati",
        "Solapur",
        "Hubli–Dharwad",
        "Chandigarh",
        "Tiruchirappalli",
        "Bareilly",
        "Moradabad",
        "Mysore",
        "Gurgaon",
        "Aligarh",
        "Jalandhar",
        "Jamshedpur",
        "Udaipur",
        "Kakinada",
        "Kollam",
        "Dehradun",
        "Vijayawada",
        "Varanasi",
        "Amritsar",
        "Aurangabad",
        "Bhubaneswar",
        "Jodhpur",
        "Raipur",
        "Guwahati",
        "Solapur",
        "Hubli–Dharwad",
        "Chandigarh",
        "Tiruchirappalli",
        "Bareilly",
        "Moradabad",
        "Mysore",
        "Gurgaon",
        "Aligarh",
        "Jalandhar",
        "Jamshedpur",
        "Udaipur",
        "Kakinada",
        "Dehradun",
        "Vijayawada",
        "Amsterdam"
    )

    val allCities = popularIndianCities + countryNames.map { countryName ->
        countryName.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }

    val detectedCompanyNames = mutableListOf<String>()
    var largestFontSize = 0f
    var nameByFontSize = ""
    var nameByUppercase = ""
    val detectedPhones = mutableListOf<String>()
    val detectedEmails = mutableListOf<String>()
    var detectedWebsite: String? = null
    var detectedCompanyDomain: String? = null
    val possibleNames = mutableListOf<String>()
    val addressLines = mutableListOf<String>()
    var detectedDesignation: String? = null
    val detectedLocationNames = mutableListOf<String>()

    for (block in visionText.textBlocks) {
        for (line in block.lines) {
            var textBlock = line.text.trim()

            if (textBlock.startsWith("•") || textBlock.startsWith("→") || textBlock.startsWith(".")) {
                textBlock = textBlock.replaceFirst(Regex("^[•→.]+"), "").trim()
                if (textBlock.isEmpty()) {
                    continue
                }
            }

            val labelValuePair = processLabelValuePair(textBlock)
            if (labelValuePair != null) {
                val (label, dataPart) = labelValuePair
                when (label.lowercase()) {
                    "phone", "mobile", "contact" -> {
                        val phoneNumbers = extractPhoneNumbers(dataPart, phoneRegex)
                        detectedPhones.addAll(phoneNumbers)
                        isBusinessCardCount += 1
                    }

                    "email" -> {
                        if (dataPart.contains("@")) {
                            detectedEmails.add(dataPart)
                            detectedCompanyDomain = dataPart.substringAfter("@")
                            isBusinessCardCount += 1
                        }
                    }

                    "website" -> {
                        if (websiteRegex.matches(dataPart)) {
                            detectedWebsite = dataPart
                            isBusinessCardCount += 1
                        }
                    }

                    "address" -> {
                        val isAddressData = addressRegex.containsMatchIn(dataPart) ||
                                postalCodeRegex.containsMatchIn(dataPart) ||
                                cityStateRegex.containsMatchIn(dataPart) ||
                                countryNames.any { dataPart.lowercase().contains(it) } ||
                                indianStates.any { dataPart.lowercase().contains(it) }
                        if (isAddressData) {
                            addressLines.add(dataPart)
                            isBusinessCardCount += 1
                        }
                    }

                    "designation", "title", "job title", "position" -> {
                        val isCity =
                            allCities.any { city -> dataPart.contains(city, ignoreCase = true) }
                        if (!addressRegex.matches(dataPart) && !isCity) { // Address and City check for designation
                            detectedDesignation = dataPart
                            isBusinessCardCount += 1
                        }
                    }
                }
                continue
            }

            if (textBlock.contains("@")) {
                detectedEmails.add(textBlock)
                detectedCompanyDomain = textBlock.substringAfter("@")
                isBusinessCardCount += 1
                continue
            }

            if (websiteRegex.matches(textBlock)) {
                detectedWebsite = textBlock
                isBusinessCardCount += 1
                continue
            }

            val sanitizedPhone = textBlock.replace(Regex("[^+\\d,]"), "")
            val phoneNumbers =
                sanitizedPhone.split(",").map { it.trim() }.filter { it.matches(phoneRegex) }
            if (phoneNumbers.isNotEmpty()) {
                detectedPhones.addAll(phoneNumbers)
                isBusinessCardCount += 1
            }

            val cityRegex =
                Regex("\\b(${popularIndianCities.joinToString("|")})\\b", RegexOption.IGNORE_CASE)

            var isAddress = addressRegex.containsMatchIn(textBlock) ||
                    postalCodeRegex.containsMatchIn(textBlock) ||
                    cityStateRegex.containsMatchIn(textBlock) ||
                    countryNames.any { country -> textBlock.lowercase().contains(country) } ||
                    indianStates.any { state -> textBlock.lowercase().contains(state) } ||
                    cityRegex.containsMatchIn(textBlock)

            var locationAdded = false

            if (!locationAdded && cityRegex.containsMatchIn(textBlock)) {
                popularIndianCities.forEach { city ->
                    if (textBlock.contains(city, ignoreCase = true)) {
                        detectedLocationNames.add(city)
                        locationAdded = true
                        return@forEach
                    }
                }
                if (locationAdded) isAddress = true
            }

            if (!locationAdded && indianStates.any { state ->
                    textBlock.lowercase().contains(state.lowercase())
                }) {
                indianStates.forEach { state ->
                    if (textBlock.lowercase().contains(state.lowercase())) {
                        detectedLocationNames.add(state)
                        locationAdded = true
                        return@forEach
                    }
                }
                if (locationAdded) isAddress = true
            }

            if (!locationAdded && countryNames.any { country ->
                    textBlock.lowercase().contains(country.lowercase())
                }) {
                countryNames.forEach { country ->
                    if (textBlock.lowercase().contains(country.lowercase())) {
                        detectedLocationNames.add(country.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.getDefault()
                            ) else it.toString()
                        }) // Keep consistent with city name capitalization
                        locationAdded = true
                        return@forEach
                    }
                }
                if (locationAdded) isAddress = true
            }

            if (isAddress) {
                addressLines.add(textBlock)
                isBusinessCardCount += 1
            }

            val fontSize = line.boundingBox?.height()?.toFloat() ?: 0f
            if (fontSize > largestFontSize) {
                largestFontSize = fontSize
                nameByFontSize = textBlock
            }

            if (textBlock.isNotEmpty() && textBlock == textBlock.uppercase()) {
                nameByUppercase = textBlock
            }

            if (
                textBlock.isNotEmpty() &&
                !textBlock.contains("@") &&
                !textBlock.contains(keywordRegex) &&
                !lineContainsPhoneNumber(textBlock, phoneRegex) &&
                !websiteRegex.matches(textBlock) &&
                !(isAddress) &&
                textBlock.matches(Regex(".*[a-zA-Z].*"))
            ) {
                possibleNames.add(textBlock)
            }

            if (textBlock.contains(keywordRegex)) {
                isBusinessCardCount += 1
            }
            sentences.add(textBlock)
        }
    }

    allTextSentences(sentences)

    if (detectedDesignation.isNullOrEmpty()) {
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                var textBlock = line.text.trim()
                if (textBlock.startsWith("•") || textBlock.startsWith("→") || textBlock.startsWith(".")) {
                    textBlock = textBlock.replaceFirst(Regex("^[•→.]+"), "").trim()
                    if (textBlock.isEmpty()) {
                        continue
                    }
                }
                if (detectedEmails.contains(textBlock) || textBlock == detectedWebsite || detectedPhones.contains(
                        textBlock
                    ) || addressLines.contains(textBlock) || possibleNames.contains(textBlock) || detectedCompanyNames.contains(
                        textBlock
                    )
                ) {
                    continue
                }

                val isCity = allCities.any { city -> textBlock.contains(city, ignoreCase = true) }

                if (detectedDesignation.isNullOrEmpty() && !addressLines.contains(textBlock) && !addressRegex.matches(
                        textBlock
                    ) && !isCity
                ) {
                    designationTerms.forEach { term ->
                        if (textBlock.contains(term, ignoreCase = true)) {
                            detectedDesignation = textBlock
                            return@forEach
                        }
                    }
                }
            }
            if (detectedDesignation != null && detectedDesignation!!.isNotEmpty()) break
        }
    }

    for (block in visionText.textBlocks) {
        for (line in block.lines) {
            var textBlock = line.text.trim()
            if (textBlock.startsWith("•") || textBlock.startsWith("→") || textBlock.startsWith(".")) {
                textBlock = textBlock.replaceFirst(Regex("^[•→.]+"), "").trim()
                if (textBlock.isEmpty()) {
                    continue
                }
            }
            if (detectedEmails.contains(textBlock) || textBlock == detectedWebsite || detectedPhones.contains(
                    textBlock
                ) || addressLines.contains(textBlock) || detectedDesignation == textBlock
            ) {
                continue
            }

            if (companyKeywordsRegex.containsMatchIn(textBlock)) {
                detectedCompanyNames.add(textBlock)
            } else {
                val corporateTerms =
                    Regex("\\b(Corp|Inc|Ltd|GmbH|Pvt\\.? LTD)\\b", RegexOption.IGNORE_CASE)
                if (corporateTerms.containsMatchIn(textBlock)) {
                    detectedCompanyNames.add(textBlock)
                }
            }
        }
    }

    possibleNames.removeAll { possibleName ->
        val companyNameMatcher = detectedCompanyNames.any { cn ->
            LevenshteinDistance.calculate(possibleName, cn) < 4 ||
                    possibleName.contains(cn, ignoreCase = true) ||
                    cn.contains(possibleName, ignoreCase = true)
        }

        val emailDomainPrefix = detectedCompanyDomain?.substringBefore(".")?.lowercase() ?: ""
        val possibleNameLower = possibleName.lowercase()

        val emailDomainMatcher = when {
            emailDomainPrefix.length > 3 -> {
                possibleNameLower.contains(emailDomainPrefix) ||
                        LevenshteinDistance.calculate(possibleNameLower, emailDomainPrefix) <= 2
            }

            emailDomainPrefix.isNotEmpty() -> {
                possibleNameLower.contains(emailDomainPrefix)
            }

            else -> false
        }

        companyNameMatcher || emailDomainMatcher
    }

    addressLines.removeAll {
        detectedCompanyNames.contains(it) || companyKeywordsRegex.containsMatchIn(it) || it == detectedDesignation
    }

    var detectedName = ""
    val emailPrefix = detectedEmails.firstOrNull()?.substringBefore("@") ?: ""
    val normalizedPrefixParts = when {
        emailPrefix.matches(Regex("^[a-zA-Z]{2,4}$")) -> {
            emailPrefix.lowercase(Locale.ROOT).map { it.toString() }
        }

        else -> {
            emailPrefix.replace(Regex("[^a-zA-Z0-9]"), " ").split(" ").map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
        }
    }

    if (detectedEmails.isNotEmpty() && normalizedPrefixParts.isNotEmpty()) {
        possibleNames.forEach { possibleName ->
            val nameParts = possibleName.split(Regex("[\\s\\-,.]+")).map { it.trim() }
                .filter { it.isNotEmpty() }
            val initials =
                nameParts.mapNotNull { part ->
                    part.firstOrNull()?.toString()
                        ?.lowercase(Locale.ROOT)
                }

            if (containsInOrder(initials, normalizedPrefixParts)) {
                detectedName = possibleName
                return@forEach
            }
        }
    }

    if (detectedName.isEmpty() || detectedName.length < 3) {
        val longerPossibleNames = possibleNames.filter { it.length >= 3 }
        detectedName = when {
            nameByFontSize.length >= 3 -> nameByFontSize
            nameByUppercase.length >= 3 -> nameByUppercase
            longerPossibleNames.isNotEmpty() -> longerPossibleNames.firstOrNull() ?: ""
            possibleNames.isNotEmpty() -> possibleNames.firstOrNull() ?: ""
            else -> ""
        }
    }

    if (detectedName.isNotEmpty()) {
        val isCompanyNameMatch = detectedCompanyNames.any { cn ->
            LevenshteinDistance.calculate(detectedName, cn) < 4 ||
                    detectedName.contains(cn, ignoreCase = true) ||
                    cn.contains(detectedName, ignoreCase = true)
        }

        val emailDomainPrefix = detectedCompanyDomain?.substringBefore(".")?.lowercase() ?: ""
        val detectedNameLower = detectedName.lowercase()

        val isEmailDomainMatch = when {
            emailDomainPrefix.length > 3 -> {
                detectedNameLower.contains(emailDomainPrefix) ||
                        LevenshteinDistance.calculate(
                            detectedNameLower,
                            emailDomainPrefix
                        ) <= 2
            }

            emailDomainPrefix.isNotEmpty() -> {
                detectedNameLower.contains(emailDomainPrefix)
            }

            else -> false
        }

        if (isCompanyNameMatch || isEmailDomainMatch) {
            detectedName = possibleNames.firstOrNull { name ->
                !detectedCompanyNames.any { cn ->
                    (LevenshteinDistance.calculate(name, cn) < 4) ||
                            name.contains(cn, ignoreCase = true) ||
                            cn.contains(name, ignoreCase = true)
                } &&
                        !(emailDomainPrefix.isNotEmpty() && name.lowercase()
                            .contains(emailDomainPrefix.lowercase())) &&
                        !name.matches(Regex("^\\d+$")) &&
                        !name.contains(
                            Regex(
                                "\\b(${countryNames.joinToString("|")})\\b",
                                RegexOption.IGNORE_CASE
                            )
                        ) &&
                        !name.contains(
                            Regex(
                                "\\b(${popularIndianCities.joinToString("|")})\\b",
                                RegexOption.IGNORE_CASE
                            )
                        )
            } ?: ""
        } else {
            detectedName = possibleNames.firstOrNull { name ->
                !name.contains(
                    Regex(
                        "\\b(${countryNames.joinToString("|")})\\b",
                        RegexOption.IGNORE_CASE
                    )
                ) &&
                        !name.contains(
                            Regex(
                                "\\b(${popularIndianCities.joinToString("|")})\\b",
                                RegexOption.IGNORE_CASE
                            )
                        )
            } ?: ""
        }
    }

    if (isBusinessCardCount > 2) {
        detectedPhones.takeIf { it.isNotEmpty() }?.let {
            details = details.copy(
                phone = it.getOrNull(0) ?: "",
                phone2 = it.getOrNull(1) ?: "",
                phone3 = it.getOrNull(2) ?: ""
            )
        }

        detectedEmails.takeIf { it.isNotEmpty() }?.let {
            details = details.copy(email = it.first().trim())
        }

        detectedWebsite?.let {
            details = details.copy(website = it)
        }

        if (detectedName.isNotEmpty()) {
            details = details.copy(name = detectedName)
        }

        detectedDesignation?.let { designation ->
            if (designation.isNotEmpty()) {
                details =
                    details.copy(designation = designation.replaceFirstChar { it.uppercaseChar() })
            }
        }

        if (detectedLocationNames.isNotEmpty()) {
            val address = when {
                popularIndianCities.any { city -> detectedLocationNames.contains(city) } -> {
                    popularIndianCities.first { city -> detectedLocationNames.contains(city) }
                        .replaceFirstChar { it.uppercaseChar() }
                }

                indianStates.any { state -> detectedLocationNames.contains(state) } -> {
                    indianStates.first { state -> detectedLocationNames.contains(state) }
                        .replaceFirstChar { it.uppercaseChar() }
                }

                countryNames.any { country ->
                    detectedLocationNames.map { it.lowercase() }.contains(country)
                } -> {
                    countryNames.first { country ->
                        detectedLocationNames.map { it.lowercase() }.contains(country)
                    }
                        .replaceFirstChar { it.uppercaseChar() }
                }

                else -> ""
            }
            if (address.isNotEmpty()) {
                details = details.copy(address = address)
            }
        }
    }

    return if (isBusinessCardCount > 2) details else BusinessCardDetails()
}

private fun extractPhoneNumbers(dataPart: String, phoneRegex: Regex): List<String> {
    val sanitizedPhone = dataPart.replace(Regex("[^+\\d,]"), "")
    return sanitizedPhone.split(",").map { it.trim() }.filter { it.matches(phoneRegex) }
}

private fun processLabelValuePair(text: String): Pair<String, String>? {
    val parts = text.split(Regex("[:\\-]"))
    if (parts.size >= 2) {
        val labelPart = parts[0].trim()
        val dataPart = parts.subList(1, parts.size).joinToString(" ").trim()
        if (labelPart.isNotBlank() && dataPart.isNotBlank()) {
            return labelPart to dataPart
        }
    }
    return null
}

object LevenshteinDistance {
    fun calculate(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) {
            for (j in 0..s2.length) {
                if (i == 0) {
                    dp[i][j] = j
                } else if (j == 0) {
                    dp[i][j] = i
                } else {
                    val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1

                    dp[i][j] = (dp[i - 1][j] + 1).coerceAtMost(dp[i][j - 1] + 1)
                        .coerceAtMost(dp[i - 1][j - 1] + cost)
                }
            }
        }

        return dp[s1.length][s2.length]
    }
}

private fun containsInOrder(initials: List<String>, prefixParts: List<String>): Boolean {
    if (prefixParts.isEmpty()) return true
    if (initials.size < prefixParts.size) return false

    for (i in 0..initials.size - prefixParts.size) {
        var match = true
        for (j in prefixParts.indices) {
            if (initials[i + j] != prefixParts[j]) {
                match = false
                break
            }
        }
        if (match) return true
    }
    return false
}

private fun lineContainsPhoneNumber(text: String, phoneRegex: Regex): Boolean {
    val sanitizedPhone = text.replace(Regex("[^+\\d,]"), "")
    val phoneNumbers = sanitizedPhone.split(",").map { it.trim() }.filter { it.matches(phoneRegex) }
    return phoneNumbers.isNotEmpty()
}