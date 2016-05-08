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

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext.Implicits.global
class GitStoreSpec extends WordSpec with Matchers with ScalaFutures {

  trait Setup {

    def store = new GitStore("/var/lib", "", "") {
      override def run(cmd: String, in: Path): Either[Failure, Success] = osRun
    }

    def osRun: Either[Failure, Success]


  }


  "GitStore.cloneRepository" should {
    "clone the repository" in new Setup {
      override val osRun = Right(Success(List()))

      val repo = store.cloneRepository("test-repo", "owner").futureValue

      repo.name should be ("test-repo")
      repo.localPath should be(Paths.get("/var/lib").resolve("test-repo"))
    }
  }

}
