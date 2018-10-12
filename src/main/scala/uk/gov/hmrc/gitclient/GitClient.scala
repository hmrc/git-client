/*
 * Copyright 2018 HM Revenue & Customs
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

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.matching.Regex

case class GitTag(name: String, createdAt: Option[ZonedDateTime])

object GitTag {

  val pattern: Regex         = "'(.*)->(.*)'".r
  val iso: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")

  def apply(s: String): GitTag = {
    val pattern(tagName, dateString) = s
    GitTag(tagName, tagDate(dateString))
  }

  def tagDate(dateString: String): Option[ZonedDateTime] =
    Try(ZonedDateTime.parse(dateString, iso)).toOption
}

trait GitClient {

  def gitStore: GitStore

  def getGitRepoTags(repoName: String, owner: String)(implicit ec: ExecutionContext): Future[List[GitTag]] =
    gitStore.cloneRepository(repoName, owner).map { repository =>
      repository.getTags
    }
}

object Git {

  def apply(localStorePath: String, apiToken: String, gitHost: String, withCleanUp: Boolean = false): GitClient =
    new GitClient {
      override val gitStore: LocalGitStore =
        if (withCleanUp)
          new LocalGitStore(localStorePath, apiToken, gitHost, FileHandler(), new OsProcess) with ScheduledCleanUp
        else
          new LocalGitStore(localStorePath, apiToken, gitHost, FileHandler(), new OsProcess)
    }
}
