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
object Versions {
    const val projectVersion = "0.1.0"

    const val spotlessPlugin = "5.6.1"
    const val ktlintPlugin = "9.4.1"
    const val protobufPlugin = "0.8.13"
    const val bintrayPlugin = "1.8.5"

    const val kotlin = "1.4.10"
    const val kotlinCoroutines = "1.3.9"
    const val ktlint = "0.39.0"
    const val protobufJava = "3.12.4"
    const val grpc = "1.34.0"
    const val grpcKotlin = "0.2.1"
    const val javaxAnnotationAPI = "1.3.+"

    const val gradleWrapper = "6.6.1"
}
