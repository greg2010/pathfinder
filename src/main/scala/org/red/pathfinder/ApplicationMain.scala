package org.red.pathfinder

import com.typesafe.scalalogging.LazyLogging




object ApplicationMain extends App with LazyLogging {
  val universeController = new UniverseController
  while (true) {
    logger.info("The route planner is ready. Please enter start system name: ")
    var start = scala.io.StdIn.readLine()
    logger.info("Enter destination system: ")
    var end = scala.io.StdIn.readLine()
    val path = universeController.getPath(start, end)
    println(path)
    println("http://evemaps.dotlan.net/route/" + path.nodes.map(_.name.replace(" ", "_")).mkString(":"))
  }
}
