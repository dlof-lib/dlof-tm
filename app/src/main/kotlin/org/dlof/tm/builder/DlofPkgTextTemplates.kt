package org.dlof.tm.builder

import org.dlof.tm.model.DlofTemplateProject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * توليد محتوى ملفات النص (set.txt / meta.json / dlotemplate.xml / Best64.xml / WQ.JSON)
 * طبقاً لمواصفة DLoF Package Formats v2.0.
 */
object DlofPkgTextTemplates {

    private fun isoNow(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date())
    }

    fun setTxt(p: DlofTemplateProject): String = buildString {
        appendLine("# ── معرّف الحزمة ──")
        appendLine("package.id=${p.packageId}")
        appendLine("package.title=${p.title}")
        appendLine("package.domain=${p.domain}")
        appendLine("package.language=${p.language}")
        appendLine("package.version=${p.version}")
        appendLine()
        appendLine("# ── تشفير ──")
        appendLine("crypto.enabled=${p.cryptoEnabled}")
        if (p.cryptoEnabled) {
            appendLine("crypto.best64Path=setting/pro/Best64.xml")
            appendLine("crypto.wqPath=setting/pro/WQ.JSON")
        }
        appendLine()
        appendLine("# ── قالب ──")
        appendLine("template.path=setting/dlotemplate.xml")
        appendLine()
        appendLine("# ── ملفات إضافية ──")
        appendLine("setting.mapFile=setting/map.dlof")
        appendLine("setting.docFile=setting/Documentation.dlof")
        appendLine("setting.licenseFile=setting/license.dlof")
        appendLine()
        appendLine("# ── وسائط ──")
        appendLine("media.imagePath=media/image")
        appendLine("media.videoPath=media/video")
        appendLine("media.fontsPath=media/fonts")
        appendLine()
        appendLine("# ── base64 ──")
        appendLine("base64.mode=${p.base64Mode.value}")
    }

    fun metaJson(p: DlofTemplateProject): String {
        val o = org.json.JSONObject()
        o.put("id", p.packageId)
        o.put("title", p.title)
        o.put("domain", p.domain)
        o.put("version", p.version)
        o.put("author", p.author)
        o.put("language", p.language)
        o.put("createdAt", isoNow())
        o.put("dlofpkg_version", "2.0")
        o.put("base64_mode", p.base64Mode.value)
        if (p.cryptoEnabled) {
            o.put("crypto_profile", p.cryptoProfile)
        }
        return o.toString(2)
    }

    fun dloTemplateXml(p: DlofTemplateProject): String = """
        |<?xml version="1.0" encoding="UTF-8"?>
        |<dlofTemplate xmlns="https://dlof.org/schema/template/1.0"
        |              id="${p.packageId}-template" name="${escapeXml(p.title)}" version="1.0">
        |  <design primaryColor="${p.primaryColor}" secondaryColor="${p.secondaryColor}"
        |          backgroundColor="${p.backgroundColor}" textColor="${p.textColor}"
        |          fontFamily="${escapeXml(p.fontFamily)}" layout="${p.layout.value}"/>
        |  <pkgSettings>
        |    <base64 encoding="${p.base64Mode.value}" default="false"/>
        |    <cryptoSettings enabled="${p.cryptoEnabled}" algorithm="AES-256-GCM"/>
        |  </pkgSettings>
        |</dlofTemplate>
    """.trimMargin()

    fun best64Xml(): String = """
        |<?xml version="1.0" encoding="UTF-8"?>
        |<CryptoProfile xmlns="https://dlof.org/schema/crypto/2.0"
        |               id="Best64" name="Best64-AES-256" version="2.0">
        |  <SymmetricEncryption>
        |    <algorithm>AES-256-GCM</algorithm>
        |    <keySize>256</keySize>
        |  </SymmetricEncryption>
        |  <KeyDerivation>
        |    <pbkdf2 algorithm="PBKDF2WithHmacSHA256" iterations="310000"/>
        |    <argon2id enabled="true" memoryKB="65536" iterations="3" parallelism="4"/>
        |  </KeyDerivation>
        |  <Options>
        |    <base64Mode>optional</base64Mode>
        |    <compressionBeforeEncrypt>true</compressionBeforeEncrypt>
        |  </Options>
        |</CryptoProfile>
    """.trimMargin()

    fun wqJson(): String {
        val o = org.json.JSONObject()
        o.put("profile", "Best64")
        o.put("quickImport", true)
        o.put("hint", "استخدم setting/pro/Best64.xml لفك تشفير هذه الحزمة")
        return o.toString(2)
    }

    fun documentationDlof(p: DlofTemplateProject): String = """
        |# ${p.title}
        |
        |تم إنشاء هذا القالب بواسطة dlof-TM.
        |المعرّف: ${p.packageId}
        |النوع: ${p.domain}
        |اللغة: ${p.language}
    """.trimMargin()

    fun licenseDlof(p: DlofTemplateProject): String = """
        |جميع الحقوق محفوظة للمؤلف: ${p.author.ifBlank { "غير محدد" }}
        |تم إنشاء هذه الحزمة عبر dlof-TM.
    """.trimMargin()

    fun mapDlof(p: DlofTemplateProject): String {
        val o = org.json.JSONObject()
        o.put("packageId", p.packageId)
        val chapters = org.json.JSONArray()
        p.imageChapters.forEach { chapters.put(it.name) }
        o.put("chapters", chapters)
        val episodes = org.json.JSONArray()
        p.videoEpisodes.forEach { episodes.put(it.name) }
        o.put("episodes", episodes)
        return o.toString(2)
    }

    /**
     * محتوى package.dlof — مستند documentLoop رئيسي يشير إلى فصول الصور
     * وحلقات الفيديو المضمَّنة في الحزمة، بحسب مخطط dlof.xsd.
     */
    fun packageDlof(p: DlofTemplateProject): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        appendLine("""<documentLoop xmlns="https://dlof.org/schema/1.0" version="1.0" id="${p.packageId}">""")
        appendLine("  <metadata>")
        appendLine("    <title>${escapeXml(p.title)}</title>")
        appendLine("    <domain>${escapeXml(p.domain)}</domain>")
        appendLine("    <author>${escapeXml(p.author)}</author>")
        appendLine("    <createdAt>${isoNow()}</createdAt>")
        appendLine("    <language>${p.language}</language>")
        appendLine("  </metadata>")
        appendLine()
        appendLine("  <loopLinks>")
        appendLine("    <loopRoot>true</loopRoot>")
        appendLine("  </loopLinks>")
        appendLine()
        appendLine("  <content>")
        if (p.imageChapters.isNotEmpty()) {
            appendLine("    <imageChapters>")
            p.imageChapters.forEach { ch ->
                appendLine("""      <chapter name="${escapeXml(ch.name)}" path="media/image/${ch.name}">""")
                ch.pages.forEach { page ->
                    appendLine("""        <page file="${escapeXml(page.fileName)}"/>""")
                }
                appendLine("      </chapter>")
            }
            appendLine("    </imageChapters>")
        }
        if (p.videoEpisodes.isNotEmpty()) {
            appendLine("    <videoEpisodes>")
            p.videoEpisodes.forEach { ep ->
                appendLine("""      <episode name="${escapeXml(ep.name)}" path="media/video/Episodes/${ep.name}">""")
                if (ep.videoFileName != null) {
                    appendLine("""        <video file="${escapeXml(ep.videoFileName)}"/>""")
                }
                if (ep.subtitleFileName != null) {
                    appendLine("""        <subtitle file="${escapeXml(ep.subtitleFileName)}"/>""")
                }
                appendLine("      </episode>")
            }
            appendLine("    </videoEpisodes>")
        }
        appendLine("  </content>")
        appendLine("</documentLoop>")
    }

    fun fontDlof(p: DlofTemplateProject): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        appendLine("""<fontRegistry xmlns="https://dlof.org/schema/1.0" version="1.0">""")
        appendLine("  <defaultFamily>${escapeXml(p.fontFamily)}</defaultFamily>")
        appendLine("  <fonts>")
        p.fonts.forEach { f ->
            appendLine("""    <font file="media/fonts/fonts/${escapeXml(f.fileName)}"/>""")
        }
        appendLine("  </fonts>")
        appendLine("</fontRegistry>")
    }

    private fun escapeXml(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    // ── Lighthouse/ — ملفات Kotlin مساعدة تُنسخ حرفياً داخل كل حزمة مُصدَّرة ──

    val lighthouseEpisodeHelper: String = """
        |package dlof.lighthouse
        |
        |/**
        | * ep.kt — مساعد الحلقات (Episode Helper)
        | * يوفر دوال مساعدة بسيطة للتنقل بين حلقات الفيديو المضمَّنة في الحزمة.
        | * تم إنشاؤه تلقائياً بواسطة dlof-TM.
        | */
        |data class EpisodeRef(val name: String, val videoPath: String, val subtitlePath: String?)
        |
        |object EpisodeHelper {
        |    fun episodePath(episodeName: String): String = "media/video/Episodes/${'$'}episodeName"
        |
        |    fun nextEpisodeName(current: String, all: List<String>): String? {
        |        val idx = all.indexOf(current)
        |        return if (idx in 0 until all.lastIndex) all[idx + 1] else null
        |    }
        |
        |    fun previousEpisodeName(current: String, all: List<String>): String? {
        |        val idx = all.indexOf(current)
        |        return if (idx > 0) all[idx - 1] else null
        |    }
        |}
    """.trimMargin()

    val lighthouseDocumentFileHelper: String = """
        |package dlof.lighthouse
        |
        |import java.io.File
        |
        |/**
        | * df.kt — مساعد الملفات (Document File Helper)
        | * دوال مساعدة للوصول إلى ملفات الحزمة (set.txt / meta.json / media) بأمان.
        | * تم إنشاؤه تلقائياً بواسطة dlof-TM.
        | */
        |object DocumentFileHelper {
        |    fun resolve(root: File, relativePath: String): File {
        |        val target = File(root, relativePath).canonicalFile
        |        val base = root.canonicalFile
        |        require(target.path.startsWith(base.path)) { "مسار غير آمن: ${'$'}relativePath" }
        |        return target
        |    }
        |
        |    fun exists(root: File, relativePath: String): Boolean =
        |        runCatching { resolve(root, relativePath).exists() }.getOrDefault(false)
        |
        |    fun readText(root: File, relativePath: String): String =
        |        resolve(root, relativePath).readText(Charsets.UTF_8)
        |}
    """.trimMargin()

    val lighthouseCryptoHelper: String = """
        |package dlof.lighthouse
        |
        |import java.security.spec.KeySpec
        |import javax.crypto.Cipher
        |import javax.crypto.SecretKeyFactory
        |import javax.crypto.spec.GCMParameterSpec
        |import javax.crypto.spec.PBEKeySpec
        |import javax.crypto.spec.SecretKeySpec
        |
        |/**
        | * cr.kt — مساعد التشفير (Crypto Helper)
        | * تطبيق مرجعي لملف تعريف Best64 (AES-256-GCM + PBKDF2-SHA256).
        | * تم إنشاؤه تلقائياً بواسطة dlof-TM — راجع setting/pro/Best64.xml للتفاصيل الكاملة.
        | */
        |object CryptoHelper {
        |    private const val ITERATIONS = 310_000
        |    private const val KEY_LENGTH = 256
        |    private const val GCM_TAG_BITS = 128
        |
        |    fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        |        val spec: KeySpec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
        |        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        |        val keyBytes = factory.generateSecret(spec).encoded
        |        return SecretKeySpec(keyBytes, "AES")
        |    }
        |
        |    fun decrypt(cipherText: ByteArray, iv: ByteArray, key: SecretKeySpec): ByteArray {
        |        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        |        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        |        return cipher.doFinal(cipherText)
        |    }
        |}
    """.trimMargin()
}
