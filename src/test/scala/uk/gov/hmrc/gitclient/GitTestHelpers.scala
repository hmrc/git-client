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

import java.nio.file.{Files, Path}

sealed trait Repo

case class InitializedRepo(name: String, path: Path) extends Repo

object GitTestHelpers extends OsProcess {

  implicit class StringToInitializedRepoImplicit(repoName: String) {

    def initRepo(directory: Path): InitializedRepo = {
      println("directory path: " + directory.toString)
      run(s"git init $repoName", directory).right
        .map(x => InitializedRepo(repoName, directory.resolve(repoName)))
        .right
        .get
    }
  }

  implicit class InitilizedRepoImplicit(repo: InitializedRepo) {

    def commitFiles(filePaths: String*): Either[Failure, InitializedRepo] = {
      filePaths.foreach { filePath =>
        createFilesWithContent(filePath, "some content")
      }

      run(s"git add .", repo.path).right
        .map { _ =>
          run(
            Array(
              "git",
              "commit",
              "-m",
              "some commit message"
            ),
            repo.path).right.map(_ => repo)
        }
        .right get
    }

    def createAnnotatedTag(tagName: String, message: String): InitializedRepo =
      run(
        Array(
          "git",
          "tag",
          "-a",
          tagName,
          "-m",
          message
        ),
        repo.path).right.map(_ => repo).right get

    def createTag(tagName: String): InitializedRepo =
      run(
        Array(
          "git",
          "tag",
          tagName
        ),
        repo.path).right.map(_ => repo).right get

    private def createFilesWithContent(filePath: String, content: String): Path = {
      val target: Path = repo.path.resolve(filePath)
      target.toFile.getParentFile.mkdirs()

      if (!target.toFile.exists()) {
        target.toFile.createNewFile()
      }

      Files.write(target, "content".getBytes("UTF-8"))
    }
  }
}
