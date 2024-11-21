package bloop.integrations.gradle.model

import bloop.integrations.gradle.tasks.PluginUtils
import org.gradle.api.Project

import scala.jdk.CollectionConverters.CollectionHasAsScala

case class BuildProjectsInfo(
    allProjects: List[Project],
    allBloopProjects: List[Project]
)

object BuildProjectsInfo {
  def collect(project: Project): BuildProjectsInfo = {
    val allProjects = project.getRootProject.getAllprojects.asScala.toList
    val bloopProjects = BuildProjectsInfo.synchronized(allProjects.filter(PluginUtils.canRunBloop))
    BuildProjectsInfo(
      allProjects,
      bloopProjects
    )
  }
}
