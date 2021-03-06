SBT Benchmark Plugin
====================
SBT plugin to support separate test sources for benchmarking. Adds support for
`src/benchmark` as additional `test` sources. 

Usage
=====
Plugins file:

    import sbt._

    class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
      val guardian = "Guardian GitHub" at "http://guardian.github.com/maven/repo-releases"
      val solr = "com.gu" % "sbt-benchmark-plugin" % "0.1"
    }


Project file:

    import sbt._
    import com.gu.benchmark.Benchmarks

    class MyProject(info: ProjectInfo) extends DefaultProject(info) with Benchmarks {
        ...
    }

Put your benchmark tests in `src/benchmark/scala`. This is used as `test` sources in
the following new actions:

 * `benchmark`: Run benchmarks in `src/benchmark`
 * `benchmark-compile`: Compile benchmarks in `src/benchmark`
 * `benchmark-only`: Runs the benchmarks provided as arguments.


Sample Benchmarks
=================

A simple benchmark test might look like the following:

    package com.gu.sample

    import org.scalatest.FunSuite
    import scala.testing.Benchmark

    case class SampleBenchmark() extends Benchmark {
      def run = {
        Thread sleep 5000
      }
    }

    class SampleBenchmarkTest extends FunSuite {
      test("SampleBenchmark") {
        val timings = SampleBenchmark() run 5
        println("Total execution time: " + timings.sum)
        println("Mean execution time: " + (timings.sum/timings.length))
      }
    }


