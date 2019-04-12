package stryker4s.run
import java.nio.file.Path
import java.util.Properties

import org.apache.maven.project.MavenProject
import org.apache.maven.shared.invoker.{DefaultInvocationRequest, InvocationRequest, Invoker}
import stryker4s.config.Config
import stryker4s.model.{Killed, Mutant, MutantRunResult, Survived}
import stryker4s.mutants.findmutants.SourceCollector
import stryker4s.report.Reporter

import scala.collection.JavaConverters._

class MavenMutantRunner(project: MavenProject, invoker: Invoker, sourceCollector: SourceCollector, reporter: Reporter)(
    implicit config: Config
) extends MutantRunner(sourceCollector, reporter) {

  private val goals = List("test").asJava

  private val properties = new Properties(project.getProperties)
  properties.setProperty("surefire.skipAfterFailureCount", 1.toString) // Stop after first failure. Only works with surefire plugin, not scalatest

  invoker.setWorkingDirectory(tmpDir.toFile)

  override def runInitialTest(workingDir: Path): Boolean = {
    val request = createRequest()

    val result = invoker.execute(request)

    result.getExitCode == 0
  }

  override def runMutant(mutant: Mutant, workingDir: Path): Path => MutantRunResult = {
    val request = createRequestWithMutation(mutant)

    val result = invoker.execute(request)

    result.getExitCode match {
      case 0 => Survived(mutant, _)
      case _ => Killed(mutant, _)
    }
  }

  private def createRequest(): InvocationRequest =
    new DefaultInvocationRequest()
      .setGoals(goals)
      .setOutputHandler(debug(_))
      .setBatchMode(true)
      .setProperties(properties)

  private def createRequestWithMutation(mutant: Mutant): InvocationRequest =
    createRequest()
      .addShellEnvironment("ACTIVE_MUTATION", String.valueOf(mutant.id))

}
