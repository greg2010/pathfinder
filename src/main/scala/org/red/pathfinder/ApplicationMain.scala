package org.red.pathfinder

import com.typesafe.scalalogging.LazyLogging
import org.red.pathfinder.Implicits._
import scala.concurrent.ExecutionContext.Implicits.global


object ApplicationMain extends App with LazyLogging {
  val universeController = new UniverseController
  while (true) {
    logger.info("The route planner is ready. Please enter start system name: ")
    var start = scala.io.StdIn.readLine()
    logger.info("Enter destination system: ")
    var end = scala.io.StdIn.readLine()
    val t0 = System.currentTimeMillis()
    val shortestRoute = universeController.getShortestPath(start, end)
    val t1 = System.currentTimeMillis()
    logger.info(s"Route built, time taken ${t1-t0}ms")
    logger.info(s"Route: ${shortestRoute.toString}")
    logger.info(s"Dotlan link: http://evemaps.dotlan.net/route/${shortestRoute.path.mkString(":")}")
  }
}
