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

package com.almightyalpaca.jetbrains.plugins.discord.plugin.notifications

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil

val notificationSettings: ApplicationNotificationSettings
    get() = service()

@State(name = "ApplicationNotificationSettings", storages = [Storage("discord.xml")])
class ApplicationNotificationSettings : PersistentStateComponent<ApplicationNotificationSettings> {
    var lastUpdateNotification: String? = null

    override fun getState(): ApplicationNotificationSettings? = this

    override fun loadState(state: ApplicationNotificationSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
