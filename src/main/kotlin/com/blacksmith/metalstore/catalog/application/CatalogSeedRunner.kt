package com.blacksmith.metalstore.catalog.application

import com.blacksmith.metalstore.auth.config.SupabaseProperties
import com.blacksmith.metalstore.catalog.config.StorageProperties
import com.blacksmith.metalstore.catalog.domain.entity.*
import com.blacksmith.metalstore.catalog.domain.repository.*
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal

@Component
@ConditionalOnProperty("app.seed.catalog.enabled", havingValue = "true", matchIfMissing = true)
class CatalogSeedRunner(
    private val familyRepo: CatalogFamilyRepository,
    private val profileRepo: CatalogProfileRepository,
    private val itemRepo: CatalogItemRepository,
    private val mapper: ObjectMapper,
    private val supabase: SupabaseProperties? = null,
    private val storageProps: StorageProperties? = null,
    @Value("\${app.seed.catalog.force:false}")
    private val force: Boolean = false
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(CatalogSeedRunner::class.java)

    override fun run(vararg args: String) {
        if (profileRepo.count() > 0) {
            if (force) {
                log.warn("Force re-seed enabled. Deleting existing catalog data...")
                clearCatalog()
            } else {
                log.info("Catalog already seeded (${profileRepo.count()} profiles). Skipping.")
                return
            }
        }

        log.info("Seeding catalog data...")
        seedFamilies()
        seedAiscShapes()
        seedEuroProfiles("data/euro/i_profiles_euro.json")
        seedEuroProfiles("data/euro/chs_profiles_euro.json")
        seedEuroProfiles("data/euro/rhs_profiles_euro.json")
        seedEuroProfiles("data/euro/shs_profiles_euro.json")
        ensureStorageBucket()
        log.info("Catalog seeding complete: ${profileRepo.count()} profiles")
    }

    @Transactional
    fun seedFamilies() {
        val aiscFamilies = listOf(
            Triple("W", "H", "Wide Flange"),
            Triple("HP", "H", "HP-bearing Pile"),
            Triple("S", "I", "American Standard Beam"),
            Triple("M", "I", "Miscellaneous Beam"),
            Triple("C", "C", "Channel"),
            Triple("MC", "C", "Miscellaneous Channel"),
            Triple("L", "L", "Angle"),
            Triple("2L", "L", "Double Angle"),
            Triple("WT", "T", "Structural Tee"),
            Triple("ST", "T", "American Standard Tee"),
            Triple("MT", "T", "Miscellaneous Tee"),
            Triple("HSS", "O", "Hollow Structural Section"),
            Triple("PIPE", "O", "Pipe")
        )
        aiscFamilies.forEach { (code, shape, desc) ->
            if (familyRepo.findByStandardAndCode("AISC", code).isEmpty) {
                familyRepo.save(CatalogFamily(standard = "AISC", code = code, shapeType = shape, description = desc))
            }
        }

        val euroFamilies = listOf(
            Triple("HEA", "H", "HE-A series"),
            Triple("HEB", "H", "HE-B series"),
            Triple("HEM", "H", "HE-M series"),
            Triple("IPE", "I", "IPE series"),
            Triple("CHS", "O", "Circular Hollow Section"),
            Triple("RHS", "R", "Rectangular Hollow Section"),
            Triple("SHS", "S", "Square Hollow Section")
        )
        euroFamilies.forEach { (code, shape, desc) ->
            if (familyRepo.findByStandardAndCode("EURO", code).isEmpty) {
                familyRepo.save(CatalogFamily(standard = "EURO", code = code, shapeType = shape, description = desc))
            }
        }
    }

    @Transactional
    fun seedAiscShapes() {
        val families = familyRepo.findAll().associateBy { it.standard + ":" + it.code }
        val text = ClassPathResource("data/aisc/Shapes-SI.csv").inputStream.bufferedReader().readText()
        val rows = text.lines().drop(1).filter { it.isNotBlank() }

        val profiles = mutableListOf<AiscProfile>()
        rows.forEach { line ->
            try {
                val cols = parseCsvLine(line)
                val type = cols.getOrElse(2) { return@forEach }
                val family = families["AISC:$type"] ?: return@forEach

                profiles.add(
                    AiscProfile(
                        family = family,
                        designation = cols.getOrElse(1) { return@forEach },
                        weightKgM = cols.getOrNull(4)?.toBigDecimalOrNull(),
                        areaCm2 = cols.getOrNull(5)?.toBigDecimalOrNull(),
                        typeNorm = type,
                        tF = cols.getOrNull(3),
                        depthMm = cols.getOrNull(6)?.toBigDecimalOrNull(),
                        flangeWidthMm = cols.getOrNull(11)?.toBigDecimalOrNull(),
                        webThicknessMm = cols.getOrNull(16)?.toBigDecimalOrNull(),
                        flangeThicknessMm = cols.getOrNull(19)?.toBigDecimalOrNull(),
                        ixCm4 = cols.getOrNull(38)?.toBigDecimalOrNull(),
                        zxCm3 = cols.getOrNull(39)?.toBigDecimalOrNull(),
                        sxCm3 = cols.getOrNull(40)?.toBigDecimalOrNull(),
                        rxCm = cols.getOrNull(41)?.toBigDecimalOrNull(),
                        iyCm4 = cols.getOrNull(42)?.toBigDecimalOrNull(),
                        zyCm3 = cols.getOrNull(43)?.toBigDecimalOrNull(),
                        syCm3 = cols.getOrNull(44)?.toBigDecimalOrNull(),
                        ryCm = cols.getOrNull(45)?.toBigDecimalOrNull(),
                        jCm4 = cols.getOrNull(49)?.toBigDecimalOrNull(),
                        cwCm6 = cols.getOrNull(50)?.toBigDecimalOrNull()
                    )
                )
            } catch (e: Exception) {
                log.warn("Skipping AISC row: ${e.message}")
            }
        }

        profileRepo.saveAll(profiles)
        log.info("  Seeded ${profiles.size} AISC profiles")
    }

    @Transactional
    fun seedEuroProfiles(path: String) {
        val families = familyRepo.findAll().associateBy { it.standard + ":" + it.code }
        val json = ClassPathResource(path).inputStream.bufferedReader().readText()
        val rawProfiles: List<Map<String, Any?>> = mapper.readValue(json, object : TypeReference<List<Map<String, Any?>>>() {})

        val profiles = mutableListOf<EuroProfile>()
        rawProfiles.forEach { raw ->
            try {
                val section = raw.getValue("Section") as? String ?: return@forEach
                val familyCode = extractEuroFamily(section) ?: return@forEach
                val family = families["EURO:$familyCode"] ?: return@forEach

                profiles.add(
                    EuroProfile(
                        family = family,
                        designation = section,
                        weightKgM = (raw["m"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        areaCm2 = (raw["A"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        heightCm = (raw["h"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        widthCm = (raw["b"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        webThicknessCm = (raw["tw"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        flangeThicknessCm = (raw["tf"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        rootRadiusCm = (raw["r"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        iyCm4 = (raw["Iy"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        iyCm = (raw["iy"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        welYCm3 = (raw["Wel_y"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        wplYCm3 = (raw["Wpl_y"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        izCm4 = (raw["Iz"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        izCm = (raw["iz"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        welZCm3 = (raw["Wel_z"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        wplZCm3 = (raw["Wpl_z"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        itCm4 = (raw["IT"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        iwCm6 = (raw["Iw"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        nplRdKn = (raw["Npl_Rd"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        vplRdZKn = (raw["Vpl_Rd_z"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        vplRdYKn = (raw["Vpl_Rd_y"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        melRdYKnm = (raw["Mel_Rd_y"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        mplRdYKnm = (raw["Mpl_Rd_y"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        melRdZKnm = (raw["Mel_Rd_z"] as? Number)?.toDouble()?.let { BigDecimal(it) },
                        mplRdZKnm = (raw["Mpl_Rd_z"] as? Number)?.toDouble()?.let { BigDecimal(it) }
                    )
                )
            } catch (e: Exception) {
                log.warn("Skipping Euro row in $path: ${e.message}")
            }
        }

        profileRepo.saveAll(profiles)
        log.info("  Seeded ${profiles.size} EURO profiles from $path")
    }

    @Transactional
    fun clearCatalog() {
        itemRepo.deleteAll()
        profileRepo.deleteAll()
        familyRepo.deleteAll()
        log.info("All catalog data cleared.")
    }

    private fun extractEuroFamily(section: String): String? {
        return when {
            section.startsWith("HEA") -> "HEA"
            section.startsWith("HEB") -> "HEB"
            section.startsWith("HEM") -> "HEM"
            section.startsWith("IPE") -> "IPE"
            section.startsWith("CHS") -> "CHS"
            section.startsWith("RHS") -> "RHS"
            section.startsWith("SHS") -> "SHS"
            else -> null
        }
    }

    private fun ensureStorageBucket() {
        if (supabase == null || supabase.url.isBlank()) return
        val bucket = storageProps?.bucketName ?: "catalog-images"
        try {
            val rest = RestTemplate()
            val headers = HttpHeaders().apply {
                set("Authorization", "Bearer ${supabase.secretKey}")
                set("Content-Type", "application/json")
            }
            val existing = rest.exchange(
                "${supabase.url}/storage/v1/bucket/$bucket",
                HttpMethod.GET,
                HttpEntity(null, headers),
                Map::class.java
            )
            log.info("Storage bucket '$bucket' already exists")
        } catch (_: Exception) {
            try {
                val rest = RestTemplate()
                val headers = HttpHeaders().apply {
                    set("Authorization", "Bearer ${supabase.secretKey}")
                    set("Content-Type", "application/json")
                }
                val body = mapOf("name" to bucket, "public" to true)
                rest.exchange(
                    "${supabase.url}/storage/v1/bucket",
                    HttpMethod.POST,
                    HttpEntity(body, headers),
                    Map::class.java
                )
                log.info("Storage bucket '$bucket' created")
            } catch (e: Exception) {
                log.warn("Could not create storage bucket '$bucket': ${e.message}")
            }
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        result.add(current.toString().trim())
        return result
    }
}
