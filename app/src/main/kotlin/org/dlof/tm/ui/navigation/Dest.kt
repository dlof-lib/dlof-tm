package org.dlof.tm.ui.navigation

object Dest {
    const val HOME = "home"
    const val EDITOR_ARG = "projectId"
    const val METADATA = "editor/{$EDITOR_ARG}/metadata"
    const val DESIGN = "editor/{$EDITOR_ARG}/design"
    const val MEDIA = "editor/{$EDITOR_ARG}/media"
    const val PACKAGE_SETTINGS = "editor/{$EDITOR_ARG}/package"
    const val EXPORT = "editor/{$EDITOR_ARG}/export"
    const val SETTINGS = "settings"

    fun metadata(id: String) = "editor/$id/metadata"
    fun design(id: String) = "editor/$id/design"
    fun media(id: String) = "editor/$id/media"
    fun packageSettings(id: String) = "editor/$id/package"
    fun export(id: String) = "editor/$id/export"
}
