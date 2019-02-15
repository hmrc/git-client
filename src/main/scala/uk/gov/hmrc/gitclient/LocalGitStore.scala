/*
 * Copyright 2019 HM Revenue & Customs
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
import java.time.Duration
import java.util.concurrent.{ScheduledFuture, ScheduledThreadPoolExecutor, TimeUnit}

import scala.concurrent.{ExecutionContext, Future}

abstract class Repository {

  def osProcess: OsProcess

  def name: String

  def localPath: Path

  def getTags: List[GitTag] =
    osProcess
      .run("git for-each-ref --sort=taggerdate --format '%(refname:short)->%(taggerdate:iso8601)' refs/tags", localPath)
      .fold(
        { f =>
          throw new RuntimeException(f.message)
        }, { s =>
          s.result.map(x => GitTag(x))
        }
      )
}

object Repository {

  def apply(repoName: String, path: String): Repository =
    new Repository {
      val osProcess       = new OsProcess
      val name: String    = repoName
      val localPath: Path = Paths.get(path)
    }
}

trait GitStore {

  def cloneRepository(repositoryName: String, owner: String)(implicit ec: ExecutionContext): Future[Repository]
  def deleteRepos(olderThan: Duration): Future[Unit]

}

private[gitclient] class LocalGitStore(
  localStorePath: String,
  apiToken: String,
  host: String,
  fileHandler: FileHandler,
  osProcess: OsProcess)
    extends GitStore {

  val storePath: Path = fileHandler.createTempDir(Paths.get(localStorePath), "git-client-store")

  def cloneRepository(repositoryName: String, owner: String)(implicit ec: ExecutionContext): Future[Repository] =
    Future {
      val temDir: Path = fileHandler.createTempDir(storePath, repositoryName)
      osProcess
        .run(s"git clone https://$apiToken:x-oauth-basic@$host/$owner/$repositoryName.git", temDir)
        .fold(
          { _ =>
            throw new RuntimeException(s"Error while cloning repository : $repositoryName owner : $owner")
          }, { s =>
            Repository(repositoryName, temDir.resolve(repositoryName).toString)
          }
        )
    }

  def deleteRepos(olderThan: Duration): Future[Unit] =
    Future.successful(fileHandler.deleteOldFiles(storePath, olderThan))

}

trait ScheduledTask {

  case class ExecutionConfig(initialDelay: Long, interval: Long, unit: TimeUnit)

  val scheduler = new ScheduledThreadPoolExecutor(1)

  def scheduledExecution[T](operation: => T)(by: => ExecutionConfig): ScheduledFuture[_] =
    scheduler.scheduleWithFixedDelay(new Runnable {
      override def run(): Unit = operation
    }, by.initialDelay, by.interval, by.unit)

}

trait ScheduledCleanUp extends ScheduledTask {
  self: GitStore =>

  def executionConfig: ExecutionConfig = ExecutionConfig(2, 2, TimeUnit.MINUTES)

  val scheduledFuture: ScheduledFuture[_] = {
    scheduledExecution(deleteRepos(Duration.ofMinutes(5)))(executionConfig)
  }
}
