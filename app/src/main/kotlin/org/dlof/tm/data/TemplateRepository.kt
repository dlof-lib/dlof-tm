package org.dlof.tm.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.dlof.tm.model.DlofTemplateProject

/**
 * طبقة وصول وحيدة للمشاريع، تُبقي قائمة محدَّثة في الذاكرة (StateFlow)
 * وتُزامنها مع التخزين المحلي (TemplateStorage).
 */
class TemplateRepository private constructor(context: Context) {

    private val storage = TemplateStorage(context.applicationContext)

    private val _projects = MutableStateFlow(storage.listProjects())
    val projects: StateFlow<List<DlofTemplateProject>> = _projects.asStateFlow()

    fun get(id: String): DlofTemplateProject? = _projects.value.firstOrNull { it.id == id }
        ?: storage.loadProject(id)

    fun save(project: DlofTemplateProject) {
        storage.saveProject(project)
        refresh()
    }

    fun delete(id: String) {
        storage.deleteProject(id)
        refresh()
    }

    fun deleteAll() {
        storage.deleteAll()
        refresh()
    }

    private fun refresh() {
        _projects.value = storage.listProjects()
    }

    companion object {
        @Volatile private var instance: TemplateRepository? = null

        fun getInstance(context: Context): TemplateRepository =
            instance ?: synchronized(this) {
                instance ?: TemplateRepository(context).also { instance = it }
            }
    }
}
