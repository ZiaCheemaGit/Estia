package com.example.estia.HomeScreen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class HomeScreenViewModel: ViewModel(){
    var userCountryName: MutableState<String> = mutableStateOf("")

    init{
        viewModelScope.launch{ getUserCountryName() }
    }

    suspend fun getUserCountryName(){
        val countryCode = getDeezerCountryIDByIP()
        userCountryName.value = getCountryNameFromCode(countryCode.toString())
    }

    suspend fun getDeezerCountryIDByIP(): String? {
        return try {
            val url = "https://ipinfo.io/json"
            val result = withContext(Dispatchers.IO) {
                URL(url).readText()
            }
            val jsonObject = JSONObject(result)
            jsonObject.getString("country") // e.g., "US"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getCountryNameFromCode(code: String): String {
        return countryCodeToNameMap[code] ?: code // fallback to code if not found
    }
}

val countryCodeToNameMap = mapOf(
    "AW" to "Aruba",
    "AF" to "Afghanistan",
    "AO" to "Angola",
    "AI" to "Anguilla",
    "AX" to "Åland Islands",
    "AL" to "Albania",
    "AD" to "Andorra",
    "AE" to "United Arab Emirates",
    "AR" to "Argentina",
    "AM" to "Armenia",
    "AS" to "American Samoa",
    "AQ" to "Antarctica",
    "AG" to "Antigua and Barbuda",
    "AU" to "Australia",
    "AT" to "Austria",
    "AZ" to "Azerbaijan",
    "BI" to "Burundi",
    "BE" to "Belgium",
    "BJ" to "Benin",
    "BF" to "Burkina Faso",
    "BD" to "Bangladesh",
    "BG" to "Bulgaria",
    "BH" to "Bahrain",
    "BS" to "Bahamas",
    "BA" to "Bosnia and Herzegovina",
    "BY" to "Belarus",
    "BZ" to "Belize",
    "BM" to "Bermuda",
    "BO" to "Bolivia",
    "BR" to "Brazil",
    "BB" to "Barbados",
    "BN" to "Brunei Darussalam",
    "BT" to "Bhutan",
    "BW" to "Botswana",
    "CF" to "Central African Republic",
    "CA" to "Canada",
    "CC" to "Cocos (Keeling) Islands",
    "CH" to "Switzerland",
    "CL" to "Chile",
    "CN" to "China",
    "CI" to "Côte d'Ivoire",
    "CM" to "Cameroon",
    "CD" to "Congo (Democratic Republic)",
    "CG" to "Congo (Republic)",
    "CO" to "Colombia",
    "KM" to "Comoros",
    "CV" to "Cabo Verde",
    "CR" to "Costa Rica",
    "CU" to "Cuba",
    "CY" to "Cyprus",
    "CZ" to "Czechia",
    "DE" to "Germany",
    "DJ" to "Djibouti",
    "DK" to "Denmark",
    "DM" to "Dominica",
    "DO" to "Dominican Republic",
    "DZ" to "Algeria",
    "EC" to "Ecuador",
    "EG" to "Egypt",
    "ER" to "Eritrea",
    "ES" to "Spain",
    "EE" to "Estonia",
    "ET" to "Ethiopia",
    "FI" to "Finland",
    "FJ" to "Fiji",
    "FR" to "France",
    "GA" to "Gabon",
    "GB" to "United Kingdom",
    "GE" to "Georgia",
    "GH" to "Ghana",
    "GN" to "Guinea",
    "GM" to "Gambia",
    "GR" to "Greece",
    "GT" to "Guatemala",
    "GY" to "Guyana",
    "HK" to "Hong Kong",
    "HN" to "Honduras",
    "HR" to "Croatia",
    "HT" to "Haiti",
    "HU" to "Hungary",
    "ID" to "Indonesia",
    "IE" to "Ireland",
    "IL" to "Israel",
    "IN" to "India",
    "IQ" to "Iraq",
    "IR" to "Iran",
    "IS" to "Iceland",
    "IT" to "Italy",
    "JM" to "Jamaica",
    "JO" to "Jordan",
    "JP" to "Japan",
    "KE" to "Kenya",
    "KG" to "Kyrgyzstan",
    "KH" to "Cambodia",
    "KR" to "South Korea",
    "KW" to "Kuwait",
    "KZ" to "Kazakhstan",
    "LA" to "Lao People's Democratic Republic",
    "LB" to "Lebanon",
    "LK" to "Sri Lanka",
    "LR" to "Liberia",
    "LS" to "Lesotho",
    "LT" to "Lithuania",
    "LU" to "Luxembourg",
    "LV" to "Latvia",
    "LY" to "Libya",
    "MA" to "Morocco",
    "MD" to "Moldova",
    "MG" to "Madagascar",
    "MM" to "Myanmar",
    "ML" to "Mali",
    "MN" to "Mongolia",
    "MR" to "Mauritania",
    "MT" to "Malta",
    "MU" to "Mauritius",
    "MV" to "Maldives",
    "MX" to "Mexico",
    "MY" to "Malaysia",
    "MZ" to "Mozambique",
    "NA" to "Namibia",
    "NE" to "Niger",
    "NG" to "Nigeria",
    "NI" to "Nicaragua",
    "NL" to "Netherlands",
    "NO" to "Norway",
    "NP" to "Nepal",
    "NZ" to "New Zealand",
    "OM" to "Oman",
    "PA" to "Panama",
    "PE" to "Peru",
    "PG" to "Papua New Guinea",
    "PH" to "Philippines",
    "PK" to "Pakistan",
    "PL" to "Poland",
    "PT" to "Portugal",
    "PY" to "Paraguay",
    "QA" to "Qatar",
    "RO" to "Romania",
    "RS" to "Serbia",
    "RU" to "Russia",
    "RW" to "Rwanda",
    "SA" to "Saudi Arabia",
    "SD" to "Sudan",
    "SE" to "Sweden",
    "SG" to "Singapore",
    "SI" to "Slovenia",
    "SK" to "Slovakia",
    "SN" to "Senegal",
    "SO" to "Somalia",
    "SR" to "Suriname",
    "SY" to "Syria",
    "TH" to "Thailand",
    "TJ" to "Tajikistan",
    "TL" to "Timor-Leste",
    "TN" to "Tunisia",
    "TR" to "Turkey",
    "TT" to "Trinidad and Tobago",
    "TW" to "Taiwan",
    "TZ" to "Tanzania",
    "UA" to "Ukraine",
    "UG" to "Uganda",
    "US" to "United States",
    "UY" to "Uruguay",
    "UZ" to "Uzbekistan",
    "VE" to "Venezuela",
    "VN" to "Vietnam",
    "YE" to "Yemen",
    "ZA" to "South Africa",
    "ZM" to "Zambia",
    "ZW" to "Zimbabwe"
)

