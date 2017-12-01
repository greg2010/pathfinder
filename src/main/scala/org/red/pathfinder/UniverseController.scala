package org.red.pathfinder

import java.util.NoSuchElementException
import java.util.concurrent.ForkJoinPool

import org.red.pathfinder.util.graph.{Path, Graph => MyGraph}
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.JdbcBackend

import scala.concurrent.duration.Duration
import slick.jdbc.PostgresProfile.api._
import org.red.db.models.Sde

import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.{Await, ExecutionContext, Future}

class UniverseController(implicit ec: ExecutionContext, dbAgent: JdbcBackend.Database) extends LazyLogging {

  val systemList: List[EveSystem] = {
    logger.info("Fetching universe information from SDE DB dump...")
    val t0 = System.currentTimeMillis()
    val q1 = Sde.MapSolarSystems.map(r => (r.solarSystemId, r.solarSystemName, r.x, r.y, r.z, r.security))
    def q2(systemId: Int) = Sde.MapSolarSystemJumps.filter(_.fromSolarSystemId === systemId).map(_.toSolarSystemId)
    val f = for {
      solarSystemList <- dbAgent.run(q1.result)
      eveSystemList <- Future.sequence {
        solarSystemList.map { ss =>
          dbAgent.run(q2(ss._1).result).map { neighbours =>
            EveSystem(ss._1, ss._2.get, Position(ss._3.get, ss._4.get, ss._5.get), neighbours.toList)
          }
        }
      }
    } yield eveSystemList.toList.filter(_.neighbours.nonEmpty)
    val res = Await.result(f, Duration.Inf)
    val t1 = System.currentTimeMillis()
    logger.info(s"Universe information fetched, time taken ${t1-t0}ms building graph...")
    res
  }

  val t0 = System.currentTimeMillis()
  val eveUniverseGraph: MyGraph[EveSystem] = createEveSystemGraph(systemList)
  val t1 = System.currentTimeMillis()
  logger.info(s"Graph built, time taken ${t1-t0}ms ...")

  private def createEveSystemGraph(systemList: List[EveSystem]): MyGraph[EveSystem] = {
    val gateGraph = systemList.foldRight(new MyGraph[EveSystem](Set()))((system, graphSoFar) =>
      system.neighbours.foldRight(graphSoFar)((neighbourId, systemGraphSoFar) =>
        systemList.find(_.id == neighbourId) match {
          case Some(neighbourSystem) => systemGraphSoFar + (system, neighbourSystem)
          case None =>
            logger.error(s"Invalid neighbour with id $neighbourId")
            systemGraphSoFar
        }
      )
    )
    val parSystemList = systemList.par
    parSystemList.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(Runtime.getRuntime.availableProcessors()))
    parSystemList.aggregate(gateGraph) ( { (graphSoFar, curSystem) =>
      val capRangeSystems =
        systemList
          .map(s => (s, curSystem.distance(s)))
          .filter(_._2 <= 6D)
      logger.info(s"Found ${capRangeSystems.length} systems in 6LY range from ${curSystem.name}")
      capRangeSystems.foldRight(graphSoFar)((systemToAdd, g) => g + (curSystem, systemToAdd._1))
    }, (g1, g2) => g1 + g2)
    gateGraph
  }

  def getShortestPath(start: String, end: String): Path[EveSystem] = {
    val systemStart = systemList.find(_.name.contains(start))
    val systemEnd = systemList.find(_.name.contains(end))
    (systemStart, systemEnd) match {
      case (Some(s), Some(e)) =>
        eveUniverseGraph.shortestPath(s, e) match {
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
