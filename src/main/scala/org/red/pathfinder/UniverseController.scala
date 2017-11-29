package org.red.pathfinder

import java.sql.Timestamp
import java.util.NoSuchElementException
import java.util.concurrent.ForkJoinPool

import com.typesafe.scalalogging.LazyLogging
import net.troja.eve.esi.api.{AllianceApi, CharacterApi, CorporationApi, UniverseApi}
import net.troja.eve.esi.model.SystemResponse

import scala.collection.JavaConverters._
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.duration.Duration
import scalax.collection.Graph
import scalax.collection.GraphEdge.UnDiEdge

class UniverseController extends LazyLogging {
  case class EveSystem(id: Int, name: String, position: Position, neighbours: List[Int])
  case class Position(x: Float, y: Float, z: Float)

  private val defaultDatasource = "tranquility"
  private val api = new UniverseApi
  logger.info("Fetching universe information from ESI API...")
  private val systemIdList = api.getUniverseSystems(defaultDatasource, null, null).asScala.toList.par
  systemIdList.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(50))
  private val systemList: List[EveSystem] = systemIdList.map(id => systemIdToEveSystem(id)).toList.filter(_.neighbours.nonEmpty)
  logger.info("Universe information fetched, building graph...")
  val eveUniverseGraph: Graph[EveSystem, UnDiEdge] = createEveSystemGraph(systemList)
  logger.info("Graph built...")

  private def createEveSystemGraph(systemList: List[EveSystem]): Graph[EveSystem, UnDiEdge] = {
    val g = Graph[EveSystem, UnDiEdge]()
    systemList.foldRight(g)((system, graphSoFar) =>
      system.neighbours.foldRight(graphSoFar)((neighbourId, systemGraphSoFar) =>
        systemList.find(_.id == neighbourId) match {
          case Some(neigbourSystem) => systemGraphSoFar + UnDiEdge(system, neigbourSystem)
          case None =>
            logger.error(s"Invalid neighbour with id $neighbourId")
            systemGraphSoFar
        }
      )
    )
  }

  private def systemIdToEveSystem(id: Int): EveSystem = {
    def stargateToNeigbour(stargateId: Int): Int = {
      val systemId = api.getUniverseStargatesStargateId(stargateId, defaultDatasource, null, null).getDestination.getSystemId
      //logger.debug(s"Processed stargate with id $stargateId, it leads to systemId $systemId")
      systemId
    }

    //logger.debug(s"Processing system with id $id")
    val res = api.getUniverseSystemsSystemId(id, defaultDatasource, null, null, null)
    //logger.debug(s"System with id $id processed, system name is ${res.getName}")
    val neigbourList = res.getStargates.asScala.par.map(stargateId => stargateToNeigbour(stargateId)).toList
    EveSystem(res.getSystemId, res.getName, Position(res.getPosition.getX, res.getPosition.getY, res.getPosition.getZ), neigbourList)
  }

  def getPath(start: String, end: String): eveUniverseGraph.Path = {
    def n(outer: EveSystem) = eveUniverseGraph.get(outer)
    val systemStart = systemList.find(_.name.contains(start))
    val systemEnd = systemList.find(_.name.contains(end))
    (systemStart, systemEnd) match {
      case (Some(s), Some(e)) =>
        val nodeStart = n(s)
        val nodeEnd = n(e)
        nodeStart.shortestPathTo(nodeEnd) match {
          case Some(p) => p
          case None =>
            logger.error(s"No path found  from start=$start end=$end")
            throw new NoSuchElementException("No path found")
        }
      case _ =>
        logger.error(s"System not found start=$start end=$end")
        throw new NoSuchElementException("System(s) not found")
    }
  }
}
