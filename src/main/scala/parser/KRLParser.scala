package parser

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Paths}

import scala.io.Source

/**
  * Created by Mnenmenth Alkaborin
  * Please refer to LICENSE file if included
  * for licensing information
  * https://github.com/Mnenmenth
  */

object Parser {
  def parse(mainToolpath: File): Unit = {
    new Thread(){override def run(): Unit ={
      new KRLParser(mainToolpath)
    }}
  }
}

class KRLParser(mainToolpath: File) {

  case class Toolpath(name: String, head: List[String], var body: List[String])

  val sourceFiles = new File(mainToolpath.getParent).listFiles.filter(_.isFile).filter(_.getName.endsWith(".SRC")).toList
  var toolpaths = List[Toolpath]()

  sourceFiles.foreach{s=>
    ui.Console.writeln(s"----START_${s.getName}----")
    ui.Console.writeln(s"Getting lines of ${s.getName}")
    val lines = Source.fromFile(s).getLines().toList
    ui.Console.writeln(s"Getting head of ${s.getName}")
    val head = lines.takeWhile(!_.contains("SetRPM"))
    ui.Console.writeln(s"Getting body of ${s.getName}")
    val body = lines.drop(head.length)
    ui.Console.writeln(s"${s.getName} parsed")
    toolpaths = toolpaths:+Toolpath(s.getName, head, body)
    ui.Console.writeln(s"----END_${s.getName}")
  }
  toolpaths = toolpaths.take(toolpaths.length-1)

  val groups = toolpaths.iterator.grouped(8).toList
  //val groupNums = Math.ceil(toolpaths.length/8).toInt
  //toolpaths.iterator.grouped(8).toList
  //ui.Console.writeln(s"Breaking toolpath into $groupNums groups")
  //for (i: Int <- 1 to groupNums) groups = groups:+toolpaths.slice((i-1)*8, i*8)

  ui.Console.writeln("Adding SetRPM(0) to the end of the last file of each group")
  groups.foreach { g =>
    ui.Console.writeln(s"Modifying ${g.last.name}")
    g.last.body = g.last.body.takeWhile(!_.toLowerCase.contentEquals("end")) ++ Seq("SetRPM(0)", "End")
  }

  var i = 1
  ui.Console.writeln(s"Getting source from ${mainToolpath.getName}")
  val mainSource = Source.fromFile(mainToolpath).getLines().filterNot(_.contentEquals("")).filterNot(_.contentEquals(" ")).toList

  groups.foreach{g=>
    ui.Console.writeln(s"----START_GROUP_$i")
    val dirName = mainToolpath.getParent+s"/Toolpath_$i"
    val dir = new File(dirName)
    if(!Files.exists(Paths.get(dirName))) dir.mkdir()
    ui.Console.writeln(s"Wrote directory: $dirName")
    var toRun = List[String]()

    g.foreach{t=>
      ui.Console.writeln(s"Writing ${t.name}")
      toRun = toRun:+t.name.split('.')(0)
      val writer = new PrintWriter(new File(dir, t.name))
      t.head.foreach(writer.println)
      t.body.foreach(writer.println)
      writer.close()
    }
    ui.Console.writeln("Writing main file for group")
    val toolpathName = groups.head.head.name.split('.')(0)
    val head = mainSource.takeWhile(!_.contains(toolpathName))
    val middle = mainSource.drop(head.length+toolpaths.length).takeWhile(!_.contains(toolpathName))
    val bottom = mainSource.drop(head.length+toolpaths.length+middle.length).takeWhile(!_.equals("End")).drop(toolpaths.length):+"End"
    val writer = new PrintWriter(new File(dir, mainToolpath.getName))
    head.foreach(writer.println)
    toRun.foreach(n=>writer.println(s"EXT $n()"))
    middle.foreach(writer.println)
    toRun.foreach(n=>writer.println(s"$n()"))
    bottom.foreach(writer.println)
    writer.close()
    ui.Console.writeln(s"----END_GROUP_$i")
    i=i+1
  }

}
