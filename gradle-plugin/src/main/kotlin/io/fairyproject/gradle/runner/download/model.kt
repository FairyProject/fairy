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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Build Response from Paper API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class BuildResponse(
  val projectId: String,
  val projectName: String,
  val version: String,
  val build: Int,
  val time: String,
  val changes: List<Change>,
  val downloads: Map<String, Download>,
  val channel: String,
  val promoted: Boolean,
)

/**
 * Change Response from Paper API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Change(
  val commit: String,
  val summary: String,
  val message: String,
)

/**
 * Download Response from Paper API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Download(
  val name: String,
  val sha256: String,
)

/**
 * Projects Response from Paper API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProjectsResponse(
  val projects: List<String>,
)

/**
 * Project Response from Paper API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProjectResponse(
  val projectId: String,
  val projectName: String,
  val versionGroups: List<String>,
  val versions: List<String>,
)

/**
 * Version Group Build Response from Paper API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class VersionGroupBuild(
  val build: Int,
  val time: String,
  val changes: List<Change>,
  val version: String,
  val downloads: Map<String, Download>,
)

/**
 * Version Group Builds Response from Paper API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class VersionGroupBuildsResponse(
  val projectId: String,
  val projectName: String,
  val versionGroup: String,
  val versions: List<String>,
  val builds: List<VersionGroupBuild>,
)

/**
 * Version Group Response from Paper API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class VersionGroupResponse(
  val projectId: String,
  val projectName: String,
  val versionGroup: String,
  val versions: List<String>,
)

/**
 * Version Response from Paper API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class VersionResponse(
  val projectId: String,
  val projectName: String,
  val version: String,
  val builds: List<Int>,
)
