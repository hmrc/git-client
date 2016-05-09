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


import java.nio.file.{Path, Paths}

import uk.gov.hmrc.gitclient.OsProcess

import scala.concurrent.{ExecutionContext, Future}


abstract class Repository {

  def osProcess: OsProcess

  def name: String

  def localPath: Path

  def getTags =
    osProcess.run("git for-each-ref --sort=taggerdate --format '%(refname:short)->%(taggerdate:iso8601)' refs/tags", localPath)
      .fold(
    { f => throw new RuntimeException(f.message) }, { s => s.result.map(x => GitTag(x)) }
    )

}

object Repository {

  def apply(repoName: String, path: String): Repository = {
    new Repository {
      val osProcess = new OsProcess
      val name: String = repoName
      val localPath: Path = Paths.get(path)
    }
  }

}

class GitStore(localStorePath: String, apiToken: String, host: String, fileHandler: FileHandler = FileHandler(), osProcess: OsProcess = new OsProcess) {

  //val git = run("which git").head.trim
  val storePath = Paths.get(localStorePath)

  def cloneRepository(repositoryName: String, owner: String)(implicit ec: ExecutionContext): Future[Repository] = {

    Future {
      val temDir: Path = fileHandler.createTemDir(storePath)
      osProcess.run(s"git clone https://$apiToken:x-oauth-basic@$host/$owner/$repositoryName.git", temDir)
        .fold(
      { f => throw new RuntimeException(f.message) }, { s => Repository(repositoryName, temDir.resolve(repositoryName).toString) })
    }
  }
}
