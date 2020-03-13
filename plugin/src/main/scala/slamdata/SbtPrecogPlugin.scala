/*
 * Copyright 2020 Precog Data
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

package precog

import sbt._, Keys._

import bintray.{BintrayKeys, BintrayPlugin}, BintrayKeys._

import sbtghactions.GitHubActionsPlugin, GitHubActionsPlugin.autoImport._

import scala.{sys, Some}
import scala.collection.immutable.Seq

object SbtPrecogPlugin extends SbtPrecogBase {

  override def requires = super.requires && BintrayPlugin

  object autoImport extends autoImport {

    lazy val noPublishSettings = Seq(
      publish := {},
      publishLocal := {},
      bintrayRelease := {},
      publishArtifact := false,
      skip in publish := true,
      bintrayEnsureBintrayPackageExists := {})
  }

  import autoImport._

  override def projectSettings =
    super.projectSettings ++
    addCommandAlias("releaseSnapshot", "; project /; reload; checkLocalEvictions; bintrayEnsureBintrayPackageExists; publish; bintrayRelease") ++
    Seq(
      sbtPlugin := true,

      bintrayOrganization := Some("precog-inc"),
      bintrayRepository := "sbt-plugins",
      bintrayReleaseOnPublish := false,

      publishMavenStyle := false,

      // it's annoying that sbt-bintray doesn't do this for us
      credentials ++= {
        if (githubIsWorkflowBuild.value) {
          val creds = for {
            user <- sys.env.get("BINTRAY_USER")
            pass <- sys.env.get("BINTRAY_PASS")
          } yield Credentials("Bintray API Realm", "api.bintray.com", user, pass)

          creds.toSeq
        } else {
          Seq()
        }
      })

  override def buildSettings =
    super.buildSettings ++
    Seq(
      secrets += file("credentials.yml.enc"),

      transferPublishAndTagResources := {
        transferToBaseDir("plugin", (ThisBuild / baseDirectory).value, "credentials.yml.enc")
        transferPublishAndTagResources.value
      })

  protected val autoImporter = autoImport
}
