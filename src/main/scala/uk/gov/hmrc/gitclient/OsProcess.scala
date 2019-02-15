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

import scala.collection.mutable.ListBuffer
import scala.sys.process.{Process, ProcessLogger}

sealed abstract class Result

case class Success(result: List[String]) extends Result

case class Failure(message: String) extends Result

private[gitclient] class OsProcess {

  def run(cmd: String): Either[Failure, Success] =
    run(cmd, Paths.get("."))

  def run(cmd: String, in: Path): Either[Failure, Success] = {

    val pb = Process(cmd, cwd = in.toFile)

    val out = ListBuffer[String]()
    val err = ListBuffer[String]()

    val logger           = ProcessLogger(s => out.append(s), e => err.append(e))
    val process: Process = pb.run(logger)
    val exitCode         = process.exitValue()
    process.destroy()

    if (exitCode != 0)
      Left(
        Failure(
          s"""
           |got exit code $exitCode from command $cmd"
           |got following errors from command $cmd \n  ${err.mkString("\n  ")}
           """.stripMargin
        ))
    else Right(Success(out.toList))
  }

  def run(cmd: Array[String]): Either[Failure, Success] =
    run(cmd, Paths.get("."))

  def run(cmd: Array[String], in: Path): Either[Failure, Success] = {

    val pb = Process(cmd, cwd = in.toFile)

    val out = ListBuffer[String]()
    val err = ListBuffer[String]()

    val logger           = ProcessLogger(s => out.append(s), e => err.append(e))
    val process: Process = pb.run(logger)
    val exitCode         = process.exitValue()
    process.destroy()

    if (exitCode != 0)
      Left(
        Failure(
          s"""
           |got exit code $exitCode from command $cmd"
           |got following errors from command $cmd \n  ${err.mkString("\n  ")}
           """.stripMargin
        ))
    else Right(Success(out.toList))
  }
}
