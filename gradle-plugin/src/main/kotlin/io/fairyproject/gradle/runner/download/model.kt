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
 * Version Response from the Paper Fill (v3) API.
 *
 * @property version the queried version metadata
 * @property builds the available build numbers, newest first
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class VersionResponse(
  val version: Version,
  val builds: List<Int>,
)

/**
 * Version metadata from the Paper Fill (v3) API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Version(
  val id: String,
)

/**
 * Build Response from the Paper Fill (v3) API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class BuildResponse(
  val id: Int,
  val time: String,
  val channel: String,
  val downloads: Map<String, Download>,
)

/**
 * Download Response from the Paper Fill (v3) API.
 *
 * @property url the direct download URL for this artifact
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Download(
  val name: String,
  val checksums: Checksums,
  val size: Long,
  val url: String,
)

/**
 * Checksums for a download from the Paper Fill (v3) API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Checksums(
  val sha256: String,
)
