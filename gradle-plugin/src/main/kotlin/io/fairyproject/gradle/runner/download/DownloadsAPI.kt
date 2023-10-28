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
 * Paper Download API.
 *
 * @since 0.7.0
 * @author Jason Penilla
 * @property endpoint API endpoint
 */
class DownloadsAPI(private val endpoint: String) {
  companion object {
    const val PAPER_ENDPOINT: String = "https://api.papermc.io/v2/"
    private val MAPPER: JsonMapper = JsonMapper.builder()
      .addModule(kotlinModule())
      .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
      .build()
  }

  private inline fun <reified R> makeQuery(query: String): R {
    val response = URL(endpoint + query).readText(Charsets.UTF_8)
    return MAPPER.readValue(response)
  }

  /**
   * Get all projects.
   */
  fun projects(): ProjectsResponse {
    return makeQuery("projects")
  }

  /**
   * Get a specific project.
   */
  fun project(projectName: String): ProjectResponse {
    return makeQuery("projects/$projectName")
  }

  /**
   * Get all versions for a project.
   */
  fun versionGroup(projectName: String, versionGroup: String): VersionGroupResponse {
    return makeQuery("projects/$projectName/version_group/$versionGroup")
  }

  /**
   * Get all builds for a project group.
   */
  fun versionGroupBuilds(projectName: String, versionGroup: String): VersionGroupBuildsResponse {
    return makeQuery("projects/$projectName/version_group/$versionGroup/builds")
  }

  /**
   * Get all builds for a project.
   */
  fun version(projectName: String, version: String): VersionResponse {
    return makeQuery("projects/$projectName/versions/$version")
  }

  /**
   * Get all downloads for a build.
   */
  fun build(projectName: String, version: String, build: Int): BuildResponse {
    return makeQuery("projects/$projectName/versions/$version/builds/$build")
  }

  /**
   * Get the download URL for a download.
   */
  fun downloadURL(projectName: String, version: String, build: Int, download: Download): String {
    return endpoint + "projects/$projectName/versions/$version/builds/$build/downloads/${download.name}"
  }
}
