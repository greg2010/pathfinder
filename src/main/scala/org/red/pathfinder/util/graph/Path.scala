package org.red.pathfinder.util.graph

import scala.util.Try

class Path[N](p: List[N]) {
  def apply(p: List[N]) = new Path(p)

  val length: Int = p.size

  val path: List[N] = p
  val last: Option[N] = p.lastOption

  def + (j: N)(implicit g: Graph[N]): Path[N] = {
    if (p.isEmpty || g.adjacent(j).contains(p.last)) this.apply(p :+ j)
    else throw new IllegalArgumentException("Bad jump addition attempt")
  }

  override def toString: String = {
    p.mkString(" => ")
  }
}