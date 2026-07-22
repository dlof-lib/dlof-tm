package org.dlof.tm.data

import android.content.Context
import org.dlof.tm.model.DlofTemplateProject
import java.io.File

/**
 * تخزين محلي بسيط لمشاريع القوالب داخل files/templates/<id>.json
 * لا حاجة لقاعدة بيانات؛ كل مشروع ملف JSON مستقل.
 */
class TemplateStorage(context: Context) {

    private val dir: File = File(context.filesDir, "templates").apply { mkdirs() }

    fun listProjects(): List<DlofTemplateProject> {
        val files = dir.listFiles { f -> f.extension == "json" } ?: emptyArray()
        return files.mapNotNull { f ->
            runCatching {
                DlofTemplateProject.fromJson(org.json.JSONObject(f.readText(Charsets.UTF_8)))
            }.getOrNull()
        }.sortedByDescending { it.updatedAt }
    }

    fun loadProject(id: String): DlofTemplateProject? {
        val file = File(dir, "$id.json")
        if (!file.exists()) return null
        return runCatching {
            DlofTemplateProject.fromJson(org.json.JSONObject(file.readText(Charsets.UTF_8)))
        }.getOrNull()
    }

    fun saveProject(project: DlofTemplateProject) {
        project.updatedAt = System.currentTimeMillis()
        val file = File(dir, "${project.id}.json")
        file.writeText(project.toJson().toString(2), Charsets.UTF_8)
    }

    fun deleteProject(id: String) {
        File(dir, "$id.json").delete()
    }

    fun deleteAll() {
        dir.listFiles()?.forEach { it.delete() }
    }
}
