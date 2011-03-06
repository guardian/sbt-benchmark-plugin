/*

   Copyright 2011 Guardian News and Media

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/
package com.gu.benchmark

import sbt._
import sbt.ScalaProject._

trait Benchmarks extends BasicWebScalaProject with MavenStyleScalaPaths {

  val DefaultBenchmarkDirectoryName = "benchmark"
  val DefaultBenchmarkCompileDirectoryName = "benchmark-classes"
  val DefaultBenchmarkAnalysisDirectoryName = "benchmark-analysis"
  val DefautBenchmarkResourcesOutputDirectoryName = "benchmark-resources"

  def benchmarkDirectoryName = DefaultBenchmarkDirectoryName
  def benchmarkCompileDirectoryName = DefaultBenchmarkCompileDirectoryName
  def benchmarkAnalysisDirectoryName = DefaultBenchmarkAnalysisDirectoryName
  def benchmarkResourcesOutputDirectoryName = DefautBenchmarkResourcesOutputDirectoryName

  def benchmarkSourcePath = sourcePath / benchmarkDirectoryName
  def benchmarkJavaSourcePath = benchmarkSourcePath / javaDirectoryName
  def benchmarkScalaSourcePath = benchmarkSourcePath / scalaDirectoryName
  def benchmarkSourceRoots = (benchmarkJavaSourcePath##) +++ (benchmarkScalaSourcePath##)
  def benchmarkCompilePath = outputPath / benchmarkCompileDirectoryName
  def benchmarkClasspath = fullClasspath(Benchmark) +++ testClasspath +++ optionalClasspath
  def benchmarkAnalysisPath = outputPath / benchmarkAnalysisDirectoryName
  def benchmarkResources = descendents(benchmarkResourcesPath ##, "*")
  def benchmarkResourcesPath = benchmarkSourcePath / resourcesDirectoryName
  def benchmarkResourcesOutputPath = outputPath / benchmarkResourcesOutputDirectoryName

  def benchmarkSources = sources(benchmarkSourceRoots)


  lazy val Benchmark = Configurations.config("benchmark") hide

  def benchmarkLabel = "benchmark"

  def benchmarkCompileOptions: Seq[CompileOption] = compileOptions
  def benchmarkJavaCompileOptions: Seq[JavaCompileOption] = javaCompileOptions

  def benchmarkTestOptions: Seq[TestOption] =
    TestListeners(benchmarkListeners) ::
    TestFilter(includeBenchmark) ::
    Nil

  def benchmarkListeners = testListeners
  def includeBenchmark(benchmark: String): Boolean = true

  class BenchmarkCompileConfig extends BaseCompileConfig {
    def baseCompileOptions = benchmarkCompileOptions
    def label = benchmarkLabel
    def sourceRoots = benchmarkSourceRoots
    def sources = benchmarkSources
    def outputDirectory = benchmarkCompilePath
    def classpath = benchmarkClasspath
    def analysisPath = benchmarkAnalysisPath
    def fingerprints = getFingerprints(testFrameworks)
    def javaOptions = javaOptionsAsString(benchmarkJavaCompileOptions)
  }

  def benchmarkCompileConfiguration: CompileConfiguration = new BenchmarkCompileConfig
  val benchmarkCompileConditional = new CompileConditional(benchmarkCompileConfiguration, buildCompiler)

  lazy val benchmarkCompile = benchmarkCompileAction
  lazy val BenchmarkCompileDescription = "Compile benchmarks in src/benchmarks"
  protected def benchmarkCompileAction = task { benchmarkCompileConditional.run } dependsOn compile describedAs BenchmarkCompileDescription

  lazy val copyBenchmarkResources = copyBenchmarkResourcesAction
  val CopyBenchmarkResourcesDescription = "Copies benchmark resources to the target directory where they can be included on the benchmark classpath."
  protected def copyBenchmarkResourcesAction = syncPathsTask(benchmarkResources, benchmarkResourcesOutputPath) describedAs CopyBenchmarkResourcesDescription

  lazy val benchmark = benchmarkAction
  lazy val BenchmarkDescription = "Run benchmarks in src/benchmarks"
  def benchmarkAction = testTask(testFrameworks, benchmarkClasspath +++ benchmarkCompilePath, benchmarkCompileConditional.analysis, benchmarkTestOptions).dependsOn(benchmarkCompile, copyResources, copyBenchmarkResources) describedAs BenchmarkDescription

  lazy val benchmarkOnly = benchmarkOnlyAction
  val BenchmarkOnlyDescription = "Runs the benchmarks provided as arguments."
  protected def benchmarkOnlyAction = benchmarkQuickMethod(benchmarkCompileConditional.analysis, benchmarkTestOptions)(options =>
                defaultBenchmarkTask(options)) describedAs(BenchmarkOnlyDescription)
  protected def benchmarkQuickMethod(benchmarkAnalysis: CompileAnalysis, options: => Seq[TestOption])(toRun: Seq[TestOption] => Task) = {
    val analysis = benchmarkAnalysis.allTests.map(_.testClassName).toList
		multiTask(analysis) { (args, includeFunction) =>
			toRun(TestArgument(args:_*) :: TestFilter(includeFunction) :: options.toList)
		}
  }

  protected def defaultBenchmarkTask(benchmarkOptions: => Seq[TestOption]) =
    testTask(testFrameworks, benchmarkClasspath +++ benchmarkCompilePath, benchmarkCompileConditional.analysis, benchmarkOptions).dependsOn(benchmarkCompile, copyResources, copyBenchmarkResources) describedAs BenchmarkDescription
}
