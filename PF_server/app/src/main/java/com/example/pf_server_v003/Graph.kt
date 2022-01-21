package com.example.pf_server_v003

import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.*
import java.util.LinkedList

data class Edge<T>(
    val source: Vertex<T>,
    val destination: Vertex<T>,
//   val weight: Double? = null
)

data class Vertex<T>(val index: Int, val data: T, val coordinate:List<Double>)

class Graph<T> {

    val adjacencies: HashMap<Vertex<T>, ArrayList<Edge<T>>> = HashMap()

    val Nodelist:MutableList<Vertex<T>> = mutableListOf()
    val cornerlist:MutableList<Vertex<T>> = mutableListOf()

    var size = -1
    var cornersize = -1

    fun createVertex(data: T, coordinate:List<Double>): Vertex<T> {
        val vertex = Vertex(adjacencies.count()+1, data, coordinate)
        adjacencies[vertex] = ArrayList()
        if(vertex.index < 9){
            Nodelist.add(vertex)
            size++
        }
        else{
            cornerlist.add(vertex)
            cornersize++
        }

        return vertex
    }

    fun addDirectedEdge(source: Vertex<T>, destination: Vertex<T>) {
        val edge = Edge(source, destination)
        adjacencies[source]?.add(edge)
    }

    fun addUndirectedEdge(source: Vertex<T>, destination: Vertex<T>) {
        addDirectedEdge(source, destination)
        addDirectedEdge(destination, source)
    }

    fun add(edge: EdgeType, source: Vertex<T>, destination: Vertex<T>) {
        when (edge) {
            EdgeType.DIRECTED -> addDirectedEdge(source, destination)
            EdgeType.UNDIRECTED -> addUndirectedEdge(source, destination)
        }
    }

    fun edges(source: Vertex<T>) = adjacencies[source] ?: arrayListOf()

//     fun weight(source: Vertex<T>, destination: Vertex<T>): Double? {
//         return edges(source).firstOrNull { it.destination == destination }?.weight
//     }

    fun addNeighbor(){
        for (i in 0..(size)){
            for(j in 0..cornersize){
                var coordinate = Nodelist[i].coordinate
                var coordinate2 = cornerlist[j].coordinate
                var vector = getVector(coordinate,coordinate2)
                if (len(vector)<1){
                    add(EdgeType.UNDIRECTED, Nodelist[i], cornerlist[j])
                }
            }
        }
        for (i in 0..(cornersize)){
            for(j in (i+1)..cornersize){
                var coordinate = cornerlist[i].coordinate
                var coordinate2 = cornerlist[j].coordinate
                var vector = getVector(coordinate,coordinate2)
                var neighbor = edges(cornerlist[i])
                var neighbor2 = edges(cornerlist[j])
                var flag = 0
                for (member in neighbor){
                    for (member2 in neighbor2){
                        if (member2.destination == member.destination){
                            flag = 1
                        }
                    }
                }
                if (len(vector)<=1 && flag == 0){
                    add(EdgeType.UNDIRECTED, cornerlist[i], cornerlist[j])
                }
            }
        }
    }

    fun bfs(source: Vertex<T>, destination: Vertex<T>): MutableMap<Vertex<T>,Vertex<T>>? {
        val queue = LinkedList<Vertex<T>>()
        val enqueued = ArrayList<Vertex<T>>()
        val visited = ArrayList<Vertex<T>>()
        var path:MutableMap<Vertex<T>, Vertex<T>> = mutableMapOf()

        queue.add(source)
        enqueued.add(source)

        while (true) {
            val vertex = queue.poll() ?: break

            visited.add(vertex)

            val neighborEdges = edges(vertex)
            neighborEdges.forEach {
                if (!enqueued.contains(it.destination)) {
                    queue.add(it.destination)
                    enqueued.add(it.destination)
                    path.put(it.destination,vertex)

                    if (it.destination == destination){
                        return path
                    }
                }
            }
        }
        return null
    }

    fun shortestpath(source: Vertex<T>, destination: Vertex<T>):ArrayDeque<Vertex<T>>?{
        val EdgeTo:MutableMap<Vertex<T>,Vertex<T>>? = bfs(source,destination)
        val stack: ArrayDeque<Vertex<T>> = ArrayDeque()
        var temp:Vertex<T> = destination

        if (EdgeTo != null){
            stack.add(temp)
            while(true){
                var node:Vertex<T>? = EdgeTo.get(temp)
                if(node == null){
                    println("no path")
                    return null
                }
                stack.add(node)
                if (node == source) return stack
                temp = node
            }
        }
        return null

    }

    // dot product
    fun dot(vector:List<Double>,vector2:List<Double>) = vector[0]*vector2[0] + vector[1]*vector2[1]

    // calculate the length of vector
    fun len(vector:List<Double>):Double = sqrt(vector[0]*vector[0] + vector[1]*vector[1])

    // get the vector of two coordinate
    fun getVector(coordinate:List<Double>, coordinate2:List<Double>):List<Double>{
        val x = coordinate2[0] - coordinate[0]
        val y = coordinate2[1] - coordinate[1]
        return listOf(x,y)
    }

    // calculate the angle with given vector and north
    fun angle(Node:Vertex<T>, Node2:Vertex<T>):Double{
        val coordinate = Node.coordinate
        val coordinate2 = Node2.coordinate
        val vector = getVector(coordinate,coordinate2)
        val North = listOf(0.0,1.0)
        val Angle = acos(dot(North,vector)/(1*len(vector))) * 180 / PI
        if (vector[0] >= 0) return Angle
        else return (360.0 - Angle)
    }

    fun calTwoNodeAngle(source:Vertex<T>,destination:Vertex<T>):MutableList<Double>?{
        val stack:ArrayDeque<Vertex<T>>? = shortestpath(source,destination)
        val Anglelist:MutableList<Double> = mutableListOf()
        if (stack != null){
            var node1:Vertex<T> = stack.removeLast()
            var node2:Vertex<T>? = stack.last()
            while(node2 != null){
                Anglelist.add(angle(node1,node2))
                node1 = stack.removeLast()
                node2 = stack.last()
                if (node2 == destination){
                    Anglelist.add(angle(node1,node2))
                    return Anglelist
                }
            }
        }
        return null
    }

    override fun toString(): String {
        return buildString { // 1
            adjacencies.forEach { (vertex, edges) -> // 2
                val edgeString = edges.joinToString { it.destination.data.toString() } // 3
                append("${vertex.data} ---> [ $edgeString ]\n") // 4
            }
        }
    }
}

enum class EdgeType {
    DIRECTED,
    UNDIRECTED
}