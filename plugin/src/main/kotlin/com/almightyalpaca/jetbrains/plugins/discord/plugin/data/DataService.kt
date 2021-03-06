/*
 * Copyright 2017-2019 Aljoscha Grebe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.almightyalpaca.jetbrains.plugins.discord.plugin.data

import com.almightyalpaca.jetbrains.plugins.discord.plugin.DiscordPlugin
import com.almightyalpaca.jetbrains.plugins.discord.plugin.settings.settings
import com.almightyalpaca.jetbrains.plugins.discord.plugin.utils.toSuspendFunction
import com.almightyalpaca.jetbrains.plugins.discord.plugin.utils.tryOrDefault
import com.almightyalpaca.jetbrains.plugins.discord.plugin.utils.tryOrNull
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.impl.EditorTabPresentationUtil
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.openapi.wm.IdeFrame

val dataService: DataService
    get() = service()

@Service
class DataService {
    suspend fun getData(): Data? = tryOrNull {
        DiscordPlugin.LOG.debug("Getting data")

        val project: Project?
        val editor: FileEditor?

        val window = IdeFocusManager.getGlobalInstance().lastFocusedIdeWindow as IdeFrame?

        if (window == null) {
            val dataManager: DataManager? = DataManager.getInstanceIfCreated()

            val dataContext = dataManager?.dataContextFromFocusAsync?.toSuspendFunction() ?: return null

            project = dataContext.getData(CommonDataKeys.PROJECT)
            editor = dataContext.getData(PlatformDataKeys.FILE_EDITOR)
        } else {
            project = window.project
            editor = when (project) {
                null -> null
                else -> FileEditorManager.getInstance(project)?.selectedEditor
            }
        }

        val applicationInfo = ApplicationInfoEx.getInstance()
        val applicationId = applicationInfo.build.productCode
        val applicationVersion = applicationInfo.fullVersion
        val applicationStartTime = ApplicationManager.getApplication().startTime
        val applicationSettings = settings

        if (project != null && project.settings.show.get()) {
            val projectName = project.name
            val projectSettings = project.settings

            if (editor != null) {
                val file = editor.file

                if (file != null) {
                    val fileName = file.name
                    val fileUniqueName = when (DumbService.isDumb(project)) {
                        true -> fileName
                        false -> ReadAction.compute<String, Exception> {
                            tryOrDefault(fileName) {
                                if (!project.isDisposed) {
                                    EditorTabPresentationUtil.getUniqueEditorTabTitle(project, file, null)
                                } else {
                                    fileName
                                }
                            }
                        }
                    }
                    val filePath = file.path
                    val fileIsWriteable = file.isWritable

                    DiscordPlugin.LOG.debug("Returning file data")

                    return Data.File(
                        applicationId,
                        applicationVersion,
                        applicationStartTime,
                        applicationSettings,
                        projectName,
                        projectSettings,
                        fileName,
                        fileUniqueName,
                        filePath,
                        fileIsWriteable
                    )
                }
            }

            DiscordPlugin.LOG.debug("Returning project data")

            return Data.Project(
                applicationId,
                applicationVersion,
                applicationStartTime,
                applicationSettings,
                projectName,
                projectSettings
            )
        }

        DiscordPlugin.LOG.debug("Returning application data")

        return Data.Application(
            applicationId,
            applicationVersion,
            applicationStartTime,
            applicationSettings
        )
    }
}
