package org.dlof.tm.builder

import android.content.ContentResolver
import android.net.Uri
import org.dlof.tm.model.DlofTemplateProject
import java.io.BufferedOutputStream
import java.io.File
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * يبني ملف .dlofpkg (ZIP) كاملاً من [DlofTemplateProject] مطابقاً لهيكل
 * المواصفة v2.0: set.txt / package.dlof / meta.json / media/ / setting/ / Lighthouse/
 */
class DlofPkgBuilder(private val resolver: ContentResolver) {

    sealed class Result {
        data class Success(val entryCount: Int) : Result()
        data class Failure(val message: String) : Result()
    }

    fun build(project: DlofTemplateProject, outputStream: OutputStream): Result {
        var count = 0
        return try {
            ZipOutputStream(BufferedOutputStream(outputStream)).use { zip ->
                zip.setLevel(9)

                writeText(zip, "set.txt", DlofPkgTextTemplates.setTxt(project)); count++
                writeText(zip, "package.dlof", DlofPkgTextTemplates.packageDlof(project)); count++
                writeText(zip, "meta.json", DlofPkgTextTemplates.metaJson(project)); count++

                writeText(zip, "setting/dlotemplate.xml", DlofPkgTextTemplates.dloTemplateXml(project)); count++
                writeText(zip, "setting/map.dlof", DlofPkgTextTemplates.mapDlof(project)); count++
                writeText(zip, "setting/Documentation.dlof", DlofPkgTextTemplates.documentationDlof(project)); count++
                writeText(zip, "setting/license.dlof", DlofPkgTextTemplates.licenseDlof(project)); count++

                if (project.cryptoEnabled) {
                    writeText(zip, "setting/pro/Best64.xml", DlofPkgTextTemplates.best64Xml()); count++
                    writeText(zip, "setting/pro/WQ.JSON", DlofPkgTextTemplates.wqJson()); count++
                }

                writeText(zip, "Lighthouse/ep.kt", DlofPkgTextTemplates.lighthouseEpisodeHelper); count++
                writeText(zip, "Lighthouse/df.kt", DlofPkgTextTemplates.lighthouseDocumentFileHelper); count++
                writeText(zip, "Lighthouse/cr.kt", DlofPkgTextTemplates.lighthouseCryptoHelper); count++

                // media/image/<chapter>/<page files>
                project.imageChapters.forEach { chapter ->
                    chapter.pages.forEach { page ->
                        val entryPath = "media/image/${chapter.name}/${page.fileName}"
                        writeUri(zip, entryPath, page.uri); count++
                    }
                }

                // media/video/Episodes/<episode>/<video + optional subtitle>
                project.videoEpisodes.forEach { ep ->
                    if (ep.videoUri != null && ep.videoFileName != null) {
                        val entryPath = "media/video/Episodes/${ep.name}/${ep.videoFileName}"
                        writeUri(zip, entryPath, ep.videoUri); count++
                    }
                    if (ep.subtitleUri != null && ep.subtitleFileName != null) {
                        val entryPath = "media/video/Episodes/${ep.name}/${ep.subtitleFileName}"
                        writeUri(zip, entryPath, ep.subtitleUri); count++
                    }
                }

                // media/fonts/fonts/*.ttf|*.otf + media/fonts/dlof/font.dlof
                if (project.fonts.isNotEmpty()) {
                    writeText(zip, "media/fonts/dlof/font.dlof", DlofPkgTextTemplates.fontDlof(project)); count++
                    project.fonts.forEach { font ->
                        val entryPath = "media/fonts/fonts/${font.fileName}"
                        writeUri(zip, entryPath, font.uri); count++
                    }
                }
            }
            Result.Success(count)
        } catch (e: Exception) {
            Result.Failure(e.message ?: "unknown error")
        }
    }

    private fun writeText(zip: ZipOutputStream, path: String, content: String) {
        zip.putNextEntry(ZipEntry(path))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun writeUri(zip: ZipOutputStream, path: String, uriString: String) {
        val uri = Uri.parse(uriString)
        zip.putNextEntry(ZipEntry(path))
        resolver.openInputStream(uri)?.use { input ->
            input.copyTo(zip)
        }
        zip.closeEntry()
    }

    companion object {
        /** اسم ملف مقترح لملف .dlofpkg الناتج بناءً على معرّف الحزمة. */
        fun suggestedFileName(project: DlofTemplateProject): String {
            val safe = project.packageId.ifBlank { "package" }
                .replace(Regex("[^A-Za-z0-9_\\-]"), "_")
            return "$safe.dlofpkg"
        }

        fun tempFile(cacheDir: File, project: DlofTemplateProject): File =
            File(cacheDir, suggestedFileName(project))
    }
}
