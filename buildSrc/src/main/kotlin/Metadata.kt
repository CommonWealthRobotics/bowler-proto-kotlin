/*
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

/**
 * This file cannot be in a package. That would prevent Gradle from loading it inside `plugins {}`
 * blocks.
 */
object Metadata {
    const val projectName = "bowler-proto-kotlin"
    const val projectDescription = "Generated Kotlin code for the bowler-proto protobuf definitions."
    const val organization = "commonwealthrobotics"
    const val groupId = "com.commonwealthrobotics"
    const val license = "Apache-2.0"
    const val githubRepo = "https://github.com/CommonWealthRobotics/bowler-proto-kotlin"
    const val scmConnection = "scm:git:git://github.com/CommonWealthRobotics/bowler-proto-kotlin.git"
    const val developerConnection = "scm:git:ssh://github.com/CommonWealthRobotics/bowler-proto-kotlin.git"
}
