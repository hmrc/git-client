/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.gitclient

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.util.matching.Regex


case class GitTag(name: String, createdAt: Option[DateTime])

object GitTag {

  val pattern: Regex = "'(.*)->(.*)'".r
  val iso = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z")

  def apply(s: String): GitTag = {
    val pattern(tagName, dateString) = s
    GitTag(tagName, tagDate(dateString))
  }

  def tagDate(dateString: String): Option[DateTime] = {
    Try(DateTime.parse(dateString, iso)).toOption
  }
}

trait GitClient {

  def git: GitStore

  def getGitRepoTags(repoName: String, owner: String)(implicit ec: ExecutionContext) =
    git.cloneRepository(repoName, owner).map { r => r.getTags }
}

object Git {

  def apply(localStorePath: String, apiToken: String, gitHost: String): GitClient = new GitClient {
    override def git: GitStore = new GitStore(localStorePath, apiToken, gitHost)
  }
}
