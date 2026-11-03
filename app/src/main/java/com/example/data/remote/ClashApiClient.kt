package com.example.data.remote

import com.example.data.model.Achievement
import com.example.data.model.BadgeUrls
import com.example.data.model.ClanDetail
import com.example.data.model.ClanMember
import com.example.data.model.CurrentWar
import com.example.data.model.CwlClan
import com.example.data.model.CwlGroup
import com.example.data.model.CwlRound
import com.example.data.model.IconUrls
import com.example.data.model.League
import com.example.data.model.PlayerClan
import com.example.data.model.PlayerDetail
import com.example.data.model.UnitItem
import com.example.data.model.WarAttack
import com.example.data.model.WarClan
import com.example.data.model.WarLogClan
import com.example.data.model.WarLogEntry
import com.example.data.model.CapitalRaidMember
import com.example.data.model.CapitalRaidSeason
import com.example.data.model.WarMember
import com.example.util.TagValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

class ClashApiClient {

    private val baseApiUrl = "https://api.clashofclans.com/v1"

    data class ConnectionTestResult(
        val success: Boolean,
        val latencyMs: Long,
        val statusCode: Int,
        val message: String,
        val errorDetails: String? = null
    )

    private fun buildOkHttpClient(
        useProxy: Boolean,
        proxyHost: String,
        proxyPort: Int,
        proxyUser: String,
        proxyPass: String
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)

        if (useProxy && proxyHost.isNotBlank() && proxyPort > 0) {
            try {
                val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyHost.trim(), proxyPort))
                builder.proxy(proxy)

                if (proxyUser.isNotBlank() || proxyPass.isNotBlank()) {
                    builder.proxyAuthenticator(object : Authenticator {
                        override fun authenticate(route: Route?, response: Response): Request? {
                            if (response.request.header("Proxy-Authorization") != null) {
                                return null // Prevent retry loop if credentials failed
                            }
                            val credential = Credentials.basic(proxyUser.trim(), proxyPass.trim())
                            return response.request.newBuilder()
                                .header("Proxy-Authorization", credential)
                                .build()
                        }
                    })
                }
            } catch (e: Exception) {
                // If proxy address invalid, fallback gracefully
            }
        }
        return builder.build()
    }

    suspend fun testConnection(
        apiKey: String,
        proxyHost: String,
        proxyPort: Int,
        proxyUser: String,
        proxyPass: String,
        useProxy: Boolean
    ): ConnectionTestResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext ConnectionTestResult(
                success = false,
                latencyMs = 0,
                statusCode = 401,
                message = "API Key is empty",
                errorDetails = "Please enter your Supercell API Key from developer.clashofclans.com in Settings."
            )
        }

        val startTime = System.currentTimeMillis()
        val client = buildOkHttpClient(useProxy, proxyHost, proxyPort, proxyUser, proxyPass)
        // Check clan #2PP (one of the oldest official clans) to test real Supercell connection
        val request = Request.Builder()
            .url("$baseApiUrl/clans/%232PP")
            .header("Authorization", "Bearer ${apiKey.trim()}")
            .header("Accept", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - startTime
                val code = response.code
                val body = response.body?.string().orEmpty()

                when (code) {
                    200 -> ConnectionTestResult(
                        success = true,
                        latencyMs = latency,
                        statusCode = 200,
                        message = "Connected to Supercell API successfully!",
                        errorDetails = "Proxy & API Key verified. Latency: ${latency}ms."
                    )
                    403 -> {
                        val reason = parseErrorMessage(body)
                        ConnectionTestResult(
                            success = false,
                            latencyMs = latency,
                            statusCode = 403,
                            message = "Access Denied (403)",
                            errorDetails = "Your Supercell API key is invalid or your IP (${if (useProxy) proxyHost else "current IP"}) is not whitelisted in developer.clashofclans.com. $reason"
                        )
                    }
                    404 -> ConnectionTestResult(
                        success = true, // Endpoint reached, key & proxy valid!
                        latencyMs = latency,
                        statusCode = 404,
                        message = "Connected to API (HTTP 404 test response)",
                        errorDetails = "API Key and network path verified."
                    )
                    429 -> ConnectionTestResult(
                        success = false,
                        latencyMs = latency,
                        statusCode = 429,
                        message = "Rate Limited (429)",
                        errorDetails = "Supercell API throttled this request. Wait a moment and retry."
                    )
                    503 -> ConnectionTestResult(
                        success = false,
                        latencyMs = latency,
                        statusCode = 503,
                        message = "Clash of Clans Maintenance (503)",
                        errorDetails = "Supercell servers are currently undergoing maintenance."
                    )
                    else -> ConnectionTestResult(
                        success = false,
                        latencyMs = latency,
                        statusCode = code,
                        message = "HTTP $code Error",
                        errorDetails = parseErrorMessage(body)
                    )
                }
            }
        } catch (e: IOException) {
            val latency = System.currentTimeMillis() - startTime
            val detail = if (useProxy) {
                "Failed to route through proxy $proxyHost:$proxyPort. Check proxy host, port, credentials, and internet: ${e.localizedMessage}"
            } else {
                "Network timeout or failure reaching api.clashofclans.com: ${e.localizedMessage}"
            }
            ConnectionTestResult(
                success = false,
                latencyMs = latency,
                statusCode = 0,
                message = "Network / Proxy Error",
                errorDetails = detail
            )
        } catch (e: Exception) {
            ConnectionTestResult(
                success = false,
                latencyMs = 0,
                statusCode = 0,
                message = "Unexpected Error",
                errorDetails = e.localizedMessage ?: "Unknown error"
            )
        }
    }

    suspend fun getPlayer(
        tag: String,
        apiKey: String,
        proxyHost: String,
        proxyPort: Int,
        proxyUser: String,
        proxyPass: String,
        useProxy: Boolean
    ): Result<Pair<PlayerDetail, String>> = withContext(Dispatchers.IO) {
        val encodedTag = TagValidator.urlEncode(tag)
        val url = "$baseApiUrl/players/$encodedTag"
        val client = buildOkHttpClient(useProxy, proxyHost, proxyPort, proxyUser, proxyPass)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${apiKey.trim()}")
            .header("Accept", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception(formatHttpError(response.code, body, tag, "Player"))
                    )
                }
                val json = JSONObject(body)
                val player = parsePlayer(json)
                Result.success(Pair(player, body))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClan(
        tag: String,
        apiKey: String,
        proxyHost: String,
        proxyPort: Int,
        proxyUser: String,
        proxyPass: String,
        useProxy: Boolean
    ): Result<Pair<ClanDetail, String>> = withContext(Dispatchers.IO) {
        val encodedTag = TagValidator.urlEncode(tag)
        val url = "$baseApiUrl/clans/$encodedTag"
        val client = buildOkHttpClient(useProxy, proxyHost, proxyPort, proxyUser, proxyPass)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${apiKey.trim()}")
            .header("Accept", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception(formatHttpError(response.code, body, tag, "Clan"))
                    )
                }
                val json = JSONObject(body)
                val clan = parseClan(json)
                Result.success(Pair(clan, body))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentWar(
        clanTag: String,
        apiKey: String,
        proxyHost: String,
        proxyPort: Int,
        proxyUser: String,
        proxyPass: String,
        useProxy: Boolean
    ): Result<CurrentWar> = withContext(Dispatchers.IO) {
        val encodedTag = TagValidator.urlEncode(clanTag)
        val url = "$baseApiUrl/clans/$encodedTag/currentwar"
        val client = buildOkHttpClient(useProxy, proxyHost, proxyPort, proxyUser, proxyPass)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${apiKey.trim()}")
            .header("Accept", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception(formatHttpError(response.code, body, clanTag, "Clan War"))
                    )
                }
                val json = JSONObject(body)
                val war = parseWar(json)
                Result.success(war)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCwlGroup(
        clanTag: String,
        apiKey: String,
        proxyHost: String,
        proxyPort: Int,
        proxyUser: String,
        proxyPass: String,
        useProxy: Boolean
    ): Result<CwlGroup> = withContext(Dispatchers.IO) {
        val encodedTag = TagValidator.urlEncode(clanTag)
        val url = "$baseApiUrl/clans/$encodedTag/currentwar/leaguegroup"
        val client = buildOkHttpClient(useProxy, proxyHost, proxyPort, proxyUser, proxyPass)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${apiKey.trim()}")
            .header("Accept", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception(formatHttpError(response.code, body, clanTag, "CWL League Group"))
                    )
                }
                val json = JSONObject(body)
                val cwl = parseCwlGroup(json)
                Result.success(cwl)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClanWarLog(
        clanTag: String,
        apiKey: String,
        proxyHost: String,
        proxyPort: Int,
        proxyUser: String,
        proxyPass: String,
        useProxy: Boolean
    ): Result<List<WarLogEntry>> = withContext(Dispatchers.IO) {
        val encodedTag = TagValidator.urlEncode(clanTag)
        val url = "$baseApiUrl/clans/$encodedTag/warlog?limit=25"
        val client = buildOkHttpClient(useProxy, proxyHost, proxyPort, proxyUser, proxyPass)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${apiKey.trim()}")
            .header("Accept", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception(formatHttpError(response.code, body, clanTag, "War Log"))
                    )
                }
                val json = JSONObject(body)
                val entries = parseWarLog(json)
                Result.success(entries)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCapitalRaidSeasons(
        clanTag: String,
        apiKey: String,
        proxyHost: String,
        proxyPort: Int,
        proxyUser: String,
        proxyPass: String,
        useProxy: Boolean
    ): Result<List<CapitalRaidSeason>> = withContext(Dispatchers.IO) {
        val encodedTag = TagValidator.urlEncode(clanTag)
        val url = "$baseApiUrl/clans/$encodedTag/capitalraidseasons?limit=10"
        val client = buildOkHttpClient(useProxy, proxyHost, proxyPort, proxyUser, proxyPass)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${apiKey.trim()}")
            .header("Accept", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception(formatHttpError(response.code, body, clanTag, "Capital Raid Seasons"))
                    )
                }
                val json = JSONObject(body)
                val seasons = parseCapitalRaidSeasons(json)
                Result.success(seasons)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatHttpError(code: Int, body: String, tag: String, target: String): String {
        val reason = parseErrorMessage(body)
        return when (code) {
            400 -> "Invalid tag format or parameters for $tag ($reason)"
            403 -> "Supercell API 403: Check API Key or IP whitelist for your proxy ($reason)"
            404 -> "$target with tag $tag not found in Clash of Clans."
            429 -> "Rate limited by Supercell API. Please wait a few seconds and try again."
            503 -> "Supercell servers are currently undergoing maintenance (503)."
            else -> "Supercell API error ($code): $reason"
        }
    }

    private fun parseErrorMessage(body: String): String {
        return try {
            val json = JSONObject(body)
            json.optString("message", json.optString("reason", body.take(120)))
        } catch (e: Exception) {
            body.take(120)
        }
    }

    // JSON PARSERS
    fun parsePlayer(json: JSONObject): PlayerDetail {
        val clanJson = json.optJSONObject("clan")
        val playerClan = if (clanJson != null) {
            val bJson = clanJson.optJSONObject("badgeUrls")
            PlayerClan(
                tag = clanJson.optString("tag", ""),
                name = clanJson.optString("name", "Unknown Clan"),
                clanLevel = clanJson.optInt("clanLevel", 1),
                badgeUrls = bJson?.let {
                    BadgeUrls(
                        small = it.optNullableString("small"),
                        medium = it.optNullableString("medium"),
                        large = it.optNullableString("large")
                    )
                }
            )
        } else null

        val leagueJson = json.optJSONObject("league")
        val league = if (leagueJson != null) {
            val icons = leagueJson.optJSONObject("iconUrls")
            League(
                id = leagueJson.optInt("id", 0),
                name = leagueJson.optString("name", "Unranked"),
                iconUrls = icons?.let {
                    IconUrls(
                        small = it.optNullableString("small"),
                        tiny = it.optNullableString("tiny"),
                        medium = it.optNullableString("medium")
                    )
                }
            )
        } else null

        return PlayerDetail(
            tag = json.optString("tag", ""),
            name = json.optString("name", "Unknown Chief"),
            townHallLevel = json.optInt("townHallLevel", 1),
            townHallWeaponLevel = json.optInt("townHallWeaponLevel", 0),
            expLevel = json.optInt("expLevel", 1),
            trophies = json.optInt("trophies", 0),
            bestTrophies = json.optInt("bestTrophies", 0),
            warStars = json.optInt("warStars", 0),
            attackWins = json.optInt("attackWins", 0),
            defenseWins = json.optInt("defenseWins", 0),
            builderHallLevel = if (json.has("builderHallLevel")) json.optInt("builderHallLevel") else null,
            builderBaseTrophies = json.optInt("builderBaseTrophies", 0),
            bestBuilderBaseTrophies = json.optInt("bestBuilderBaseTrophies", 0),
            donations = json.optInt("donations", 0),
            donationsReceived = json.optInt("donationsReceived", 0),
            role = json.optNullableString("role"),
            warPreference = json.optNullableString("warPreference"),
            clan = playerClan,
            league = league,
            heroes = parseUnits(json.optJSONArray("heroes")),
            heroEquipment = parseUnits(json.optJSONArray("heroEquipment")),
            troops = parseUnits(json.optJSONArray("troops")),
            spells = parseUnits(json.optJSONArray("spells")),
            achievements = parseAchievements(json.optJSONArray("achievements"))
        )
    }

    private fun parseUnits(array: JSONArray?): List<UnitItem> {
        if (array == null) return emptyList()
        val list = mutableListOf<UnitItem>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            list.add(
                UnitItem(
                    name = obj.optString("name", "Unit"),
                    level = obj.optInt("level", 1),
                    maxLevel = obj.optInt("maxLevel", 1),
                    village = obj.optString("village", "home")
                )
            )
        }
        return list
    }

    private fun parseAchievements(array: JSONArray?): List<Achievement> {
        if (array == null) return emptyList()
        val list = mutableListOf<Achievement>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            list.add(
                Achievement(
                    name = obj.optString("name", "Achievement"),
                    stars = obj.optInt("stars", 0),
                    value = obj.optLong("value", 0L),
                    target = obj.optLong("target", 0L),
                    info = obj.optString("info", ""),
                    completionInfo = obj.optNullableString("completionInfo")
                )
            )
        }
        return list
    }

    fun parseClan(json: JSONObject): ClanDetail {
        val bJson = json.optJSONObject("badgeUrls")
        val badge = bJson?.let {
            BadgeUrls(
                small = it.optNullableString("small"),
                medium = it.optNullableString("medium"),
                large = it.optNullableString("large")
            )
        }

        val locationObj = json.optJSONObject("location")
        val location = locationObj?.optNullableString("name")

        val membersArray = json.optJSONArray("memberList")
        val membersList = mutableListOf<ClanMember>()
        if (membersArray != null) {
            for (i in 0 until membersArray.length()) {
                val m = membersArray.optJSONObject(i) ?: continue
                val lObj = m.optJSONObject("league")
                val league = lObj?.let {
                    val icons = it.optJSONObject("iconUrls")
                    League(
                        id = it.optInt("id", 0),
                        name = it.optString("name", "Unranked"),
                        iconUrls = icons?.let { ic ->
                            IconUrls(
                                small = ic.optNullableString("small"),
                                tiny = ic.optNullableString("tiny"),
                                medium = ic.optNullableString("medium")
                            )
                        }
                    )
                }

                membersList.add(
                    ClanMember(
                        tag = m.optString("tag", ""),
                        name = m.optString("name", "Chief"),
                        role = m.optString("role", "member"),
                        expLevel = m.optInt("expLevel", 1),
                        league = league,
                        trophies = m.optInt("trophies", 0),
                        builderBaseTrophies = m.optInt("builderBaseTrophies", 0),
                        clanRank = m.optInt("clanRank", i + 1),
                        previousClanRank = m.optInt("previousClanRank", 0),
                        donations = m.optInt("donations", 0),
                        donationsReceived = m.optInt("donationsReceived", 0),
                        townHallLevel = m.optInt("townHallLevel", 0)
                    )
                )
            }
        }

        val wlObj = json.optJSONObject("warLeague")
        val warLeague = wlObj?.optNullableString("name")

        return ClanDetail(
            tag = json.optString("tag", ""),
            name = json.optString("name", "Unknown Clan"),
            type = json.optString("type", "open"),
            description = json.optString("description", ""),
            location = location,
            badgeUrls = badge,
            clanLevel = json.optInt("clanLevel", 1),
            clanPoints = json.optInt("clanPoints", 0),
            clanBuilderBasePoints = json.optInt("clanBuilderBasePoints", 0),
            clanCapitalPoints = json.optInt("clanCapitalPoints", 0),
            capitalHallLevel = json.optJSONObject("clanCapital")?.optInt("capitalHallLevel", 0) ?: 0,
            requiredTrophies = json.optInt("requiredTrophies", 0),
            warFrequency = json.optNullableString("warFrequency"),
            warWinStreak = json.optInt("warWinStreak", 0),
            warWins = json.optInt("warWins", 0),
            warTies = json.optInt("warTies", 0),
            warLosses = json.optInt("warLosses", 0),
            isWarLogPublic = json.optBoolean("isWarLogPublic", false),
            warLeague = warLeague,
            members = json.optInt("members", membersList.size),
            memberList = membersList
        )
    }

    private fun parseWar(json: JSONObject): CurrentWar {
        val state = json.optString("state", "notInWar")
        if (state == "notInWar") {
            return CurrentWar(state = "notInWar")
        }

        val clanJson = json.optJSONObject("clan")
        val oppJson = json.optJSONObject("opponent")

        return CurrentWar(
            state = state,
            teamSize = json.optInt("teamSize", 0),
            attacksPerMember = json.optInt("attacksPerMember", 1),
            battleModifier = json.optNullableString("battleModifier"),
            preparationStartTime = json.optNullableString("preparationStartTime"),
            startTime = json.optNullableString("startTime"),
            endTime = json.optNullableString("endTime"),
            clan = clanJson?.let { parseWarClan(it) },
            opponent = oppJson?.let { parseWarClan(it) }
        )
    }

    private fun parseWarClan(json: JSONObject): WarClan {
        val bJson = json.optJSONObject("badgeUrls")
        val badge = bJson?.let {
            BadgeUrls(
                small = it.optNullableString("small"),
                medium = it.optNullableString("medium"),
                large = it.optNullableString("large")
            )
        }

        val membersArray = json.optJSONArray("members")
        val membersList = mutableListOf<WarMember>()
        if (membersArray != null) {
            for (i in 0 until membersArray.length()) {
                val m = membersArray.optJSONObject(i) ?: continue
                val attacksArray = m.optJSONArray("attacks")
                val attacks = mutableListOf<WarAttack>()
                if (attacksArray != null) {
                    for (j in 0 until attacksArray.length()) {
                        val a = attacksArray.optJSONObject(j) ?: continue
                        attacks.add(
                            WarAttack(
                                attackerTag = a.optString("attackerTag", ""),
                                defenderTag = a.optString("defenderTag", ""),
                                stars = a.optInt("stars", 0),
                                destructionPercentage = a.optDouble("destructionPercentage", 0.0),
                                order = a.optInt("order", 0),
                                duration = a.optInt("duration", 0)
                            )
                        )
                    }
                }

                val bestAttObj = m.optJSONObject("bestOpponentAttack")
                val bestAttack = bestAttObj?.let {
                    WarAttack(
                        attackerTag = it.optString("attackerTag", ""),
                        defenderTag = it.optString("defenderTag", ""),
                        stars = it.optInt("stars", 0),
                        destructionPercentage = it.optDouble("destructionPercentage", 0.0),
                        order = it.optInt("order", 0),
                        duration = it.optInt("duration", 0)
                    )
                }

                membersList.add(
                    WarMember(
                        tag = m.optString("tag", ""),
                        name = m.optString("name", "Chief"),
                        mapPosition = m.optInt("mapPosition", 1),
                        townhallLevel = m.optInt("townhallLevel", 1),
                        attacks = attacks,
                        opponentAttacks = m.optInt("opponentAttacks", 0),
                        bestOpponentAttack = bestAttack
                    )
                )
            }
        }

        return WarClan(
            tag = json.optString("tag", ""),
            name = json.optString("name", "Clan"),
            badgeUrls = badge,
            clanLevel = json.optInt("clanLevel", 1),
            attacks = json.optInt("attacks", 0),
            stars = json.optInt("stars", 0),
            destructionPercentage = json.optDouble("destructionPercentage", 0.0),
            members = membersList.sortedBy { it.mapPosition }
        )
    }

    private fun parseCwlGroup(json: JSONObject): CwlGroup {
        val state = json.optString("state", "notInWar")
        val season = json.optString("season", "")

        val clansArray = json.optJSONArray("clans")
        val clans = mutableListOf<CwlClan>()
        if (clansArray != null) {
            for (i in 0 until clansArray.length()) {
                val c = clansArray.optJSONObject(i) ?: continue
                val b = c.optJSONObject("badgeUrls")
                clans.add(
                    CwlClan(
                        tag = c.optString("tag", ""),
                        name = c.optString("name", "Clan"),
                        clanLevel = c.optInt("clanLevel", 1),
                        badgeUrls = b?.let {
                            BadgeUrls(
                                small = it.optNullableString("small"),
                                medium = it.optNullableString("medium"),
                                large = it.optNullableString("large")
                            )
                        }
                    )
                )
            }
        }

        val roundsArray = json.optJSONArray("rounds")
        val rounds = mutableListOf<CwlRound>()
        if (roundsArray != null) {
            for (i in 0 until roundsArray.length()) {
                val r = roundsArray.optJSONObject(i) ?: continue
                val warTagsArray = r.optJSONArray("warTags")
                val tags = mutableListOf<String>()
                if (warTagsArray != null) {
                    for (j in 0 until warTagsArray.length()) {
                        val wt = warTagsArray.optString(j, "")
                        if (wt.isNotBlank() && wt != "#0") {
                            tags.add(wt)
                        }
                    }
                }
                rounds.add(CwlRound(warTags = tags))
            }
        }

        return CwlGroup(state = state, season = season, clans = clans, rounds = rounds)
    }

    fun parseWarLog(json: JSONObject): List<WarLogEntry> {
        val items = json.optJSONArray("items") ?: return emptyList()
        val list = mutableListOf<WarLogEntry>()
        for (i in 0 until items.length()) {
            val obj = items.optJSONObject(i) ?: continue
            val clanObj = obj.optJSONObject("clan") ?: JSONObject()
            val oppObj = obj.optJSONObject("opponent") ?: JSONObject()

            val clan = WarLogClan(
                tag = clanObj.optNullableString("tag"),
                name = clanObj.optNullableString("name"),
                clanLevel = clanObj.optInt("clanLevel", 1),
                attacks = clanObj.optInt("attacks", 0),
                stars = clanObj.optInt("stars", 0),
                destructionPercentage = clanObj.optDouble("destructionPercentage", 0.0),
                expEarned = clanObj.optInt("expEarned", 0),
                badgeUrls = clanObj.optJSONObject("badgeUrls")?.let { b ->
                    BadgeUrls(b.optNullableString("small"), b.optNullableString("medium"), b.optNullableString("large"))
                }
            )

            val opp = WarLogClan(
                tag = oppObj.optNullableString("tag"),
                name = oppObj.optNullableString("name"),
                clanLevel = oppObj.optInt("clanLevel", 1),
                attacks = oppObj.optInt("attacks", 0),
                stars = oppObj.optInt("stars", 0),
                destructionPercentage = oppObj.optDouble("destructionPercentage", 0.0),
                expEarned = oppObj.optInt("expEarned", 0),
                badgeUrls = oppObj.optJSONObject("badgeUrls")?.let { b ->
                    BadgeUrls(b.optNullableString("small"), b.optNullableString("medium"), b.optNullableString("large"))
                }
            )

            list.add(
                WarLogEntry(
                    result = obj.optNullableString("result"),
                    endTime = obj.optNullableString("endTime"),
                    teamSize = obj.optInt("teamSize", 0),
                    attacksPerMember = obj.optInt("attacksPerMember", 1),
                    battleModifier = obj.optNullableString("battleModifier"),
                    clan = clan,
                    opponent = opp
                )
            )
        }
        return list
    }

    fun parseCapitalRaidSeasons(json: JSONObject): List<CapitalRaidSeason> {
        val items = json.optJSONArray("items") ?: return emptyList()
        val list = mutableListOf<CapitalRaidSeason>()
        for (i in 0 until items.length()) {
            val obj = items.optJSONObject(i) ?: continue
            val membersArr = obj.optJSONArray("members") ?: JSONArray()
            val memberList = mutableListOf<CapitalRaidMember>()
            for (m in 0 until membersArr.length()) {
                val mObj = membersArr.optJSONObject(m) ?: continue
                memberList.add(
                    CapitalRaidMember(
                        tag = mObj.optString("tag", ""),
                        name = mObj.optString("name", "Unknown Member"),
                        attacks = mObj.optInt("attacks", 0),
                        attackLimit = mObj.optInt("attackLimit", 5),
                        bonusAttackLimit = mObj.optInt("bonusAttackLimit", 1),
                        capitalResourcesLooted = mObj.optInt("capitalResourcesLooted", 0)
                    )
                )
            }
            memberList.sortByDescending { it.capitalResourcesLooted }

            list.add(
                CapitalRaidSeason(
                    state = obj.optString("state", ""),
                    startTime = obj.optString("startTime", ""),
                    endTime = obj.optString("endTime", ""),
                    capitalTotalLoot = obj.optInt("capitalTotalLoot", 0),
                    raidsCompleted = obj.optInt("raidsCompleted", 0),
                    totalAttacks = obj.optInt("totalAttacks", 0),
                    enemyDistrictsDestroyed = obj.optInt("enemyDistrictsDestroyed", 0),
                    offensiveReward = obj.optInt("offensiveReward", 0),
                    defensiveReward = obj.optInt("defensiveReward", 0),
                    members = memberList
                )
            )
        }
        return list
    }

    private fun JSONObject.optNullableString(key: String): String? =
        if (has(key) && !isNull(key)) optString(key) else null
}
