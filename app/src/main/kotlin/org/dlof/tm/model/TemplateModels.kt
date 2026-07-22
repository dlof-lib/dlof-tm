package org.dlof.tm.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * صورة واحدة داخل فصل (chapter) من فصول القصة المصورة.
 * uri: مسار المحتوى (content://) الذي اختاره المستخدم من المعرض.
 */
data class ImagePage(
    val uri: String,
    val fileName: String
)

/** فصل صور: مجموعة صفحات مرتبطة باسم فصل واحد (media/image/chapterN/). */
data class ImageChapter(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val pages: List<ImagePage> = emptyList()
)

/** حلقة فيديو واحدة: فيديو + ترجمة اختيارية (media/video/Episodes/episodeN/). */
data class VideoEpisode(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val videoUri: String?,
    val videoFileName: String?,
    val subtitleUri: String?,
    val subtitleFileName: String?
)

/** ملف خط (media/fonts/fonts/*.ttf|*.otf). */
data class FontFile(
    val uri: String,
    val fileName: String
)

enum class Base64Mode(val value: String) {
    OPTIONAL("optional"), ALWAYS("always"), NEVER("never");
    companion object {
        fun from(value: String) = entries.firstOrNull { it.value == value } ?: OPTIONAL
    }
}

enum class TemplateLayout(val value: String) {
    STANDARD("standard"), CARD("card"), MAGAZINE("magazine"), MINIMAL("minimal");
    companion object {
        fun from(value: String) = entries.firstOrNull { it.value == value } ?: STANDARD
    }
}

/**
 * مشروع قالب dlof-TM الكامل — يمثل حزمة .dlofpkg واحدة قيد الإعداد.
 * يُحفظ محلياً كـ JSON، ويُصدَّر لاحقاً كملف ZIP بامتداد .dlofpkg
 * مطابق لمواصفة DLoF Package Formats v2.0.
 */
data class DlofTemplateProject(
    val id: String = UUID.randomUUID().toString(),
    var packageId: String = "my-package",
    var title: String = "",
    var domain: String = "series",
    var author: String = "",
    var language: String = "ar",
    var version: String = "2.0",

    // design (setting/dlotemplate.xml)
    var primaryColor: String = "#1E8E3E",
    var secondaryColor: String = "#34C759",
    var backgroundColor: String = "#FFFFFF",
    var textColor: String = "#101410",
    var fontFamily: String = "Cairo",
    var layout: TemplateLayout = TemplateLayout.STANDARD,

    // package settings (set.txt)
    var base64Mode: Base64Mode = Base64Mode.OPTIONAL,
    var cryptoEnabled: Boolean = false,
    var cryptoProfile: String = "Best64",
    var compressBeforeEncrypt: Boolean = true,

    // media
    var imageChapters: List<ImageChapter> = emptyList(),
    var videoEpisodes: List<VideoEpisode> = emptyList(),
    var fonts: List<FontFile> = emptyList(),

    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        val o = JSONObject()
        o.put("id", id)
        o.put("packageId", packageId)
        o.put("title", title)
        o.put("domain", domain)
        o.put("author", author)
        o.put("language", language)
        o.put("version", version)
        o.put("primaryColor", primaryColor)
        o.put("secondaryColor", secondaryColor)
        o.put("backgroundColor", backgroundColor)
        o.put("textColor", textColor)
        o.put("fontFamily", fontFamily)
        o.put("layout", layout.value)
        o.put("base64Mode", base64Mode.value)
        o.put("cryptoEnabled", cryptoEnabled)
        o.put("cryptoProfile", cryptoProfile)
        o.put("compressBeforeEncrypt", compressBeforeEncrypt)
        o.put("createdAt", createdAt)
        o.put("updatedAt", updatedAt)

        val chapters = JSONArray()
        imageChapters.forEach { ch ->
            val co = JSONObject()
            co.put("id", ch.id)
            co.put("name", ch.name)
            val pages = JSONArray()
            ch.pages.forEach { p ->
                val po = JSONObject()
                po.put("uri", p.uri)
                po.put("fileName", p.fileName)
                pages.put(po)
            }
            co.put("pages", pages)
            chapters.put(co)
        }
        o.put("imageChapters", chapters)

        val episodes = JSONArray()
        videoEpisodes.forEach { ep ->
            val eo = JSONObject()
            eo.put("id", ep.id)
            eo.put("name", ep.name)
            eo.put("videoUri", ep.videoUri ?: JSONObject.NULL)
            eo.put("videoFileName", ep.videoFileName ?: JSONObject.NULL)
            eo.put("subtitleUri", ep.subtitleUri ?: JSONObject.NULL)
            eo.put("subtitleFileName", ep.subtitleFileName ?: JSONObject.NULL)
            episodes.put(eo)
        }
        o.put("videoEpisodes", episodes)

        val fontsArr = JSONArray()
        fonts.forEach { f ->
            val fo = JSONObject()
            fo.put("uri", f.uri)
            fo.put("fileName", f.fileName)
            fontsArr.put(fo)
        }
        o.put("fonts", fontsArr)

        return o
    }

    companion object {
        fun fromJson(o: JSONObject): DlofTemplateProject {
            val chapters = mutableListOf<ImageChapter>()
            val chArr = o.optJSONArray("imageChapters")
            if (chArr != null) {
                for (i in 0 until chArr.length()) {
                    val co = chArr.getJSONObject(i)
                    val pages = mutableListOf<ImagePage>()
                    val pArr = co.optJSONArray("pages")
                    if (pArr != null) {
                        for (j in 0 until pArr.length()) {
                            val po = pArr.getJSONObject(j)
                            pages.add(ImagePage(po.getString("uri"), po.getString("fileName")))
                        }
                    }
                    chapters.add(ImageChapter(co.getString("id"), co.getString("name"), pages))
                }
            }

            val episodes = mutableListOf<VideoEpisode>()
            val epArr = o.optJSONArray("videoEpisodes")
            if (epArr != null) {
                for (i in 0 until epArr.length()) {
                    val eo = epArr.getJSONObject(i)
                    episodes.add(
                        VideoEpisode(
                            id = eo.getString("id"),
                            name = eo.getString("name"),
                            videoUri = eo.optString("videoUri").takeIf { it.isNotEmpty() && it != "null" },
                            videoFileName = eo.optString("videoFileName").takeIf { it.isNotEmpty() && it != "null" },
                            subtitleUri = eo.optString("subtitleUri").takeIf { it.isNotEmpty() && it != "null" },
                            subtitleFileName = eo.optString("subtitleFileName").takeIf { it.isNotEmpty() && it != "null" }
                        )
                    )
                }
            }

            val fonts = mutableListOf<FontFile>()
            val fArr = o.optJSONArray("fonts")
            if (fArr != null) {
                for (i in 0 until fArr.length()) {
                    val fo = fArr.getJSONObject(i)
                    fonts.add(FontFile(fo.getString("uri"), fo.getString("fileName")))
                }
            }

            return DlofTemplateProject(
                id = o.getString("id"),
                packageId = o.optString("packageId", "my-package"),
                title = o.optString("title", ""),
                domain = o.optString("domain", "series"),
                author = o.optString("author", ""),
                language = o.optString("language", "ar"),
                version = o.optString("version", "2.0"),
                primaryColor = o.optString("primaryColor", "#1E8E3E"),
                secondaryColor = o.optString("secondaryColor", "#34C759"),
                backgroundColor = o.optString("backgroundColor", "#FFFFFF"),
                textColor = o.optString("textColor", "#101410"),
                fontFamily = o.optString("fontFamily", "Cairo"),
                layout = TemplateLayout.from(o.optString("layout", "standard")),
                base64Mode = Base64Mode.from(o.optString("base64Mode", "optional")),
                cryptoEnabled = o.optBoolean("cryptoEnabled", false),
                cryptoProfile = o.optString("cryptoProfile", "Best64"),
                compressBeforeEncrypt = o.optBoolean("compressBeforeEncrypt", true),
                imageChapters = chapters,
                videoEpisodes = episodes,
                fonts = fonts,
                createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
            )
        }
    }
}
