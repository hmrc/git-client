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

import java.nio.file.{Files, Path}

import org.joda.time.DateTime
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.gitclient.GitTestHelpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class GitClientSpec extends WordSpec with Matchers with ScalaFutures with DefaultPatienceConfig with MockitoSugar {


  val directory: Path = Files.createTempDirectory("local-git-store")
  val gitStore = mock[GitStore]

  val client = new GitClient {
    override def git: GitStore = gitStore
  }


  "GitClient.getGitRepoTags" should {
    "get all tag names and tagger date" in {

      val repo = "testing-repo" initRepo directory
      repo.commitFiles ("eventualTags.txt")
      repo createAnnotatedTag("v1.0.0", "first tag")

      when(gitStore.cloneRepository("testing-repo","owner")).thenReturn(Future.successful(Repository("testing-repo", directory.resolve("testing-repo").toString)))

      val tags: List[GitTag] = client.getGitRepoTags("testing-repo", "owner").futureValue

      tags.size should be(1)
      tags.head.name should be("v1.0.0")
      tags.head.createdAt.formatted("yyyy-MM-dd") should be(DateTime.now().formatted("yyyy-MM-dd"))

    }
  }

}
