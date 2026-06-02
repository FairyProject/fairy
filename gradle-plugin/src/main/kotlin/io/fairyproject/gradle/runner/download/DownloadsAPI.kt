/*
 * Run Task Gradle Plugins
 * Copyright (c) 2023 Jason Penilla
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fairyproject.gradle.runner.download

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URL

/**
 * Paper Fill (v3) Download API.
 *
 * @since 0.7.0
 * @author Jason Penilla
 * @property endpoint API endpoint
 */
class DownloadsAPI(private val endpoint: String) {
  companion object {
    const val PAPER_ENDPOINT: String = "https://fill.papermc.io/v3/"

    /**
     * Download key for the default server jar in a build's downloads map.
     */
    const val SERVER_DOWNLOAD: String = "server:default"

    /**
     * Fill requires a descriptive User-Agent, otherwise requests may be rejected.
     */
    private const val USER_AGENT: String = "fairy-gradle-plugin"

    private val MAPPER: JsonMapper = JsonMapper.builder()
      .addModule(kotlinModule())
      .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
      .build()
  }

  private inline fun <reified R> makeQuery(query: String): R {
    val connection = URL(endpoint + query).openConnection()
    connection.setRequestProperty("User-Agent", USER_AGENT)
    val response = connection.getInputStream().use { it.readBytes().toString(Charsets.UTF_8) }
    return MAPPER.readValue(response)
  }

  /**
   * Get a version, including the list of available builds (newest first).
   */
  fun version(projectName: String, version: String): VersionResponse {
    return makeQuery("projects/$projectName/versions/$version")
  }

  /**
   * Get the downloads for a specific build.
   */
  fun build(projectName: String, version: String, build: Int): BuildResponse {
    return makeQuery("projects/$projectName/versions/$version/builds/$build")
  }

  /**
   * Get the downloads for the latest build of a version.
   */
  fun latestBuild(projectName: String, version: String): BuildResponse {
    return makeQuery("projects/$projectName/versions/$version/builds/latest")
  }
}
