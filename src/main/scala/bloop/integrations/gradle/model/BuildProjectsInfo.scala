package bloop.integrations.gradle.model

import bloop.integrations.gradle.tasks.PluginUtils
import org.gradle.api.Project

import java.nio.file.Path
import scala.jdk.CollectionConverters.CollectionHasAsScala

final case class BuildProjectsInfo(
    allProjects: List[Project],
    allBloopProjects: List[Project]
) {
  val bloopProjectByName: Map[String, List[Project]] = allBloopProjects.groupBy(_.getName)

  private def getCommonRootPath(path1: Path, path2: Path): Path = {
    var idx = 0
    var finished = false
    while (!finished) {
      if (
        idx < path1.getNameCount() && idx < path2.getNameCount() && path1
          .getName(idx) == path2
          .getName(idx)
      )
        idx = idx + 1
      else
        finished = true
    }
    path1.getRoot().resolve(path1.subpath(0, idx))
  }

  val workspacePath: Path = {
    allProjects
      .map(_.getProjectDir().toPath())
      .foldLeft(allProjects.head.getRootDir().toPath())(getCommonRootPath)
  }
}

object BuildProjectsInfo {
  private val PropertyHolder: String = BuildProjectsInfo.getClass.getName

  def collect(project: Project): BuildProjectsInfo = BuildProjectsInfo.synchronized {
    if (project.getRootProject.hasProperty(PropertyHolder)) {
      project.getRootProject.getProperties.get(PropertyHolder).asInstanceOf[BuildProjectsInfo]
    } else {
      val allProjects = project.getRootProject.getAllprojects.asScala.toList
      val bloopProjects = BuildProjectsInfo.synchronized(allProjects.filter(PluginUtils.canRunBloop))
      val info = new BuildProjectsInfo(
        allProjects,
        bloopProjects
      )
      project.getRootProject.getProperties.asInstanceOf[java.util.Map[String, Object]].put(PropertyHolder, info)
      info
    }
  }
}
