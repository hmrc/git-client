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
import java.time.Duration

import org.mockito
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito
import org.mockito.Mockito.{when, verify}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext.Implicits.global

class LocalGitStoreSpec extends WordSpec with Matchers with ScalaFutures with MockitoSugar {

  val storePath: String = "/some/path"

  trait Setup {
    val fileHandler = mock[FileHandler]
    val osProcess = mock[OsProcess]

    def store = new LocalGitStore(storePath, "token", "github.com", fileHandler, osProcess)
  }


  "LocalGitStore.cloneRepository" should {
    "return repository with correct local path" in new Setup {


      private val repoPath: Path = Paths.get(s"$storePath/random")
      when(fileHandler.createTemDir(Paths.get(storePath))).thenReturn(repoPath)
      when(osProcess.run(anyString(), any[Path])).thenReturn(Right(Success(List())))

      val repo = store.cloneRepository("test-repo", "owner").futureValue

      repo.name should be("test-repo")
      repo.localPath should be(Paths.get("/some/path/random").resolve("test-repo"))
    }

    "run the correct git clone command" in new Setup {

      private val repoPath: Path = Paths.get(s"$storePath/random")
      when(fileHandler.createTemDir(Paths.get(storePath))).thenReturn(repoPath)
      when(osProcess.run(anyString(), any[Path])).thenReturn(Right(Success(List())))

      val repo = store.cloneRepository("test-repo", "owner").futureValue

      verify(osProcess).run("git clone https://token:x-oauth-basic@github.com/owner/test-repo.git", repoPath)
    }
  }

  "LocalGitStore.deleteRepos" should {
    "delete repos older than the given duration" in new Setup {

      store.deleteRepos(Duration.ofMinutes(5))

      Mockito.verify(fileHandler).deleteOldFiles(Paths.get(storePath), Duration.ofMinutes(5))

    }

  }
}
