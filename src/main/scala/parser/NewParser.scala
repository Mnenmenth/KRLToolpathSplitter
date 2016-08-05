package parser

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Paths}

import scala.io.Source

/**
  * Created by Razim on 8/4/2016.
  */
class NewParser(mainToolpath: File) {

  case class Toolpath(name: String, head: List[String], var body: List[String])

  val sourceFiles = new File(mainToolpath.getParent).listFiles.filter(_.isFile).filter(_.getName.endsWith(".SRC")).toList
  var toolpaths = List[Toolpath]()

  sourceFiles.foreach{s=>
    val lines = Source.fromFile(s).getLines().toList
    val head = lines.takeWhile(!_.contains("SetRPM"))
    val body = lines.drop(head.length)
    toolpaths = toolpaths:+Toolpath(s.getName, head, body)
  }
  toolpaths = toolpaths.take(toolpaths.length-1)

  var groups = List[List[Toolpath]]()
  val groupNums = Math.ceil(toolpaths.length/8).toInt
  println(groupNums)

  for (i: Int <- 1 to groupNums) groups = groups:+toolpaths.slice((i-1)*8, i*8)

  groups.foreach(g=> g.last.body = g.last.body.takeWhile(!_.equals("End")) ++ Seq("SetRPM(0)", "End"))

  var i = 1
  val mainSource = Source.fromFile(mainToolpath).getLines().toList
  groups.foreach{g=>
    val dirName = mainToolpath.getParent+s"/Toolpath_$i"
    val dir = new File(dirName)
    if(!Files.exists(Paths.get(dirName))) dir.mkdir()
    var toRun = List[String]()
    g.foreach{t=>
      toRun = toRun:+t.name.split('.')(0)
      val writer = new PrintWriter(new File(dir, t.name))
      t.head.foreach(writer.println)
      t.body.foreach(writer.println)
      writer.close()
    }

    val head = mainSource.takeWhile(!_.contains(g.head.name.split('.')(0)))
    val middle = mainSource.drop(head.length+toolpaths.length).takeWhile(!_.contains(g.head.name.split('.')(0)))
    val bottom = mainSource.drop(head.length+toolpaths.length+middle.length).takeWhile(!_.equals("End")).drop(toolpaths.length):+"End"
    val writer = new PrintWriter(new File(dir, "mainTest"))
    head.foreach(writer.println)
    toRun.foreach(n=>writer.println(s"EXT $n()"))
    middle.foreach(writer.println)
    toRun.foreach(n=>"$n()")
    bottom.foreach(writer.println)
    writer.close()
    i=i+1
  }
  println(i)

}
