package stryker4s.config

import better.files._
import pureconfig.ConfigWriter

case class Config(mutate: Seq[String] = Seq("**/main/scala/**/*.scala"),
                  baseDir: File = File.currentWorkingDirectory,
                  testRunner: TestRunner = CommandRunner("sbt", "test"),
                  reporters: Seq[ReporterType] = Seq(ConsoleReporterType),
                  files: Option[Seq[String]] = None,
                  excludedMutations: ExcludedMutations = ExcludedMutations(),
                  thresholds: Thresholds = Thresholds()) {

  def toHoconString: String = {
    import stryker4s.config.implicits.ConfigWriterImplicits._
    import pureconfig.generic.auto._

    ConfigWriter[Config].to(this).render(options)
  }
}
