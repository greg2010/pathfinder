package org.red.pathfinder.util.graph

import de.ummels.prioritymap.PriorityMap
import org.red.pathfinder.util.Util._


class Graph[N](val edgeSet: Set[(N, N)]) {
  implicit val ctx: Graph[N] = this
  val nodes: Set[N] = edgeSet.map(_._1) ++ edgeSet.map(_._2)

  def apply(edgeSet: Set[(N, N)]): Graph[N] = new Graph(edgeSet)

  def adjacent(node: N): Set[N] = {
    nodes.find(_ == node) match {
      case Some(n) => edgeSet.filter(e => e._1 == node || e._2 == node).flatMap(e => Set(e._1, e._2))
      case None => throw new IllegalArgumentException("Node is not in the graph")
    }
  }

  def +(edge: (N, N)): Graph[N] = {
    edgeSet.find(e => e == edge || e.swap == edge) match {
      case Some(_) => this
      case None => this.apply(edgeSet + edge)
    }
  }

  def +(other: Graph[N]): Graph[N] = {
    new Graph[N](this.edgeSet ++ other.edgeSet)
  }

  def dijkstra(source: N): (Map[N, Int], Map[N, N]) = {
    def go(active: PriorityMap[N, Int], acc: Map[N, Int], pred: Map[N, N]):
    (Map[N, Int], Map[N, N]) =
      if (active.isEmpty) (acc, pred)
      else {
        val (node, cost) = active.head
        val neighbours = for {
          (n, c) <- this.adjacent(node).map(n => (n, 1)).toMap if !acc.contains(n) && cost + c < active.getOrElse(n, Int.MaxValue)
        } yield n -> (cost + c)
        val preds = neighbours mapValues (_ => node)
        go(active.tail ++ neighbours, acc + (node -> cost), pred ++ preds)
      }

    go(PriorityMap(source -> 0), Map.empty, Map.empty)
  }

  def shortestPath(source: N, target: N): Option[Path[N]] = {
    val pred = dijkstra(source)._2
    if (pred.contains(target) || source == target) {
      val p = new Path[N](List())
      Some(iterateRight(target)(pred.get).foldLeft(p)((l, r) =>
        l + r
      ))
    }
    else None
  }
}