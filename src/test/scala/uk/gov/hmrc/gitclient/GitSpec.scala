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

import org.scalatest.{Matchers, WordSpec}

class GitSpec extends WordSpec with Matchers {

  "Git.apply" should {
    "create correct GitStore instance" in {
      Git("", "", "", true).gitStore.isInstanceOf[LocalGitStore with ScheduledCleanUp] should be(true)
      Git("", "", "", false).gitStore.isInstanceOf[LocalGitStore]                      should be(true)
    }
  }
}
