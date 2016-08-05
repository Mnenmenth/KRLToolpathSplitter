package parser

import java.io.{PrintWriter, File}
import java.nio.file.{Paths, Files}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source


/**
  * Created by Mnenmenth Alkaborin
  * Please refer to LICENSE file if included
  * for licensing information
  * https://github.com/Mnenmenth
  */

object Parser{
  def parse(mainFile: File): Unit ={
    new Thread(){override def run(){
      val parser = new Parser(mainFile)
      parser.writeOut()
      ui.Console.writeln("Finished")
      join()
    }}.start()
  }
}

class Parser(var mainFile: File) {

  //val nl = System.getProperty("line.separator")
  val nl = "\r\n"

  val toolpathName: String = mainFile.getName.split("_")(0)

  val mainTop = s"&ACCESS RVP${nl}&REL 1${nl}&PARAM TEMPLATE = C:\\KRC\\Roboter\\Template\\vorgabe${nl}&PARAM EDITMASK = *${nl}DEF ${toolpathName}_Kuka_APT2_Main()${nl}"
  val goHomeAndInit = s";FOLD INI;%{PE}%V3.2.0,%MKUKATPBASIS,%CINIT,%VCOMMON,%P${nl}  ;FOLD BAS INI;%{E}%V3.2.0,%MKUKATPBASIS,%CINIT,%VINIT,%P${nl}    GLOBAL INTERRUPT DECL 3 WHEN $$STOPMESS==TRUE DO IR_STOPM ( )${nl}    INTERRUPT ON 3${nl}    BAS (#INITMOV,0 )${nl}  ;ENDFOLD (BAS INI)${nl}  ;FOLD USER INI;%{E}%V3.2.0,%MKUKATPUSER,%CINIT,%VINIT,%P${nl}    ;Make your modifications here${nl}  ;ENDFOLD (USER INI)${nl};ENDFOLD (INI)${nl} ${nl};FOLD PTP HOME  Vel= 100 % DEFAULT;%{PE}%V3.2.0,%MKUKATPBASIS,%CMOVE,%VPTP,%P 1:PTP, 2:HOME, 3:, 5:100, 7:DEFAULT${nl}  $$BWDSTART = FALSE${nl}  $$H_POS=XHOME${nl}  PDAT_ACT = PDEFAULT${nl}  BAS (#PTP_DAT )${nl}  FDAT_ACT = FHOME${nl}  BAS (#FRAMES )${nl}  BAS (#VEL_PTP,100 )${nl}  PTP XHOME${nl};ENDFOLD${nl} ${nl} ${nl};*** Make Initial Position Move ${nl};FOLD PTP PPI1  Vel= 100 % PDAT1 Tool[0] Base[0];%{PE}%R 5.2.22,%MKUKATPBASIS,%CMOVE,%VPTP,%P 1:PTP, 2:PPI1, 3:, 5:100, 7:PDAT1${nl}  $$BWDSTART = FALSE${nl}  PDAT_ACT = PPDAT1${nl}  FDAT_ACT = FPPI1${nl}  BAS(#PTP_PARAMS,100)${nl}  PTP XPPI1${nl};ENDFOLD"
  val mainBottom = s"HALT${nl} ${nl};FOLD PTP HOME  Vel= 100 % DEFAULT;%{PE}%V3.2.0,%MKUKATPBASIS,%CMOVE,%VPTP,%P 1:PTP, 2:HOME, 3:, 5:100, 7:DEFAULT${nl}  $$BWDSTART = FALSE${nl} $$H_POS=XHOME${nl}  PDAT_ACT = PDEFAULT${nl}  BAS (#PTP_DAT )${nl}  FDAT_ACT = FHOME${nl}  BAS (#FRAMES )${nl}  BAS (#VEL_PTP,100 )${nl}  PTP XHOME${nl};ENDFOLD${nl} ${nl}End"


  val path = mainFile.getAbsolutePath.replace(mainFile.getName, "")

  val lines = Source.fromFile(mainFile).getLines()

  val files: mutable.LinkedHashMap[Int, (File, String)] = {
    val filez = new mutable.LinkedHashMap[Int, (File, String)]()
    var fileNum = -1
    lines.foreach(l=>if(l.contains(toolpathName))fileNum+=1)
    println("Adding Main: " + new File(path + toolpathName + "_Kuka_APT2_Main.SRC").getAbsolutePath)
    filez.put(0, (new File(path + toolpathName + "_Kuka_APT2_Main.SRC"), ""))
    for(i <- 1 to (fileNum/2)) {
      var num = ""
      if(i < 10) num = "0" + i else num = i.toString
      println("Adding File: " + path+toolpathName+"_Kuka_APT2"+num+"T02S"+num+".SRC")
      filez.put(i, (new File(path+toolpathName+"_Kuka_APT2"+num+"T02S"+num+".SRC"), num))
    }
    filez
  }

  val tool: Int = {
    var tool = 0
    Source.fromFile(files.getOrElse(1, (new File("null"), ""))._1).getLines().foreach{l=>
      if(l.contains("ToolChange")) tool = l.replace("(", "").replace(")", "").split(",1")(0).split("ToolChange")(1).toInt
    }
    tool
  }

  val rpm: Int = {
    if(tool == 1) 10000
    else if(tool == 2) 8000
    else if(tool == 3) 6000
    else 0
  }

  def toolPathFirstTop(num: String) = s"&ACCESS RVP${nl}&REL 2${nl}&PARAM TEMPLATE = C:\\KRC\\Roboter\\Template\\vorgabe${nl}&PARAM EDITMASK = *${nl}DEF ${toolpathName}_Kuka_APT2${num}T02S${num}()${nl} ${nl} ${nl};FOLD INI;%{PE}%V3.2.0,%MKUKATPBASIS,%CINIT,%VCOMMON,%P${nl}  ;FOLD BAS INI;%{E}%V3.2.0,%MKUKATPBASIS,%CINIT,%VINIT,%P${nl}    GLOBAL INTERRUPT DECL 3 WHEN $$STOPMESS==TRUE DO IR_STOPM ( )${nl}    INTERRUPT ON 3${nl}    BAS (#INITMOV,0 )${nl}  ;ENDFOLD (BAS INI)${nl}  ;FOLD USER INI;%{E}%V3.2.0,%MKUKATPUSER,%CINIT,%VINIT,%P${nl}    ;Make your modifications here${nl}  ;ENDFOLD (USER INI)${nl};ENDFOLD (INI)${nl} ${nl} ${nl} ${nl};*** Make Initial Position Move ${nl};FOLD PTP PPI1  Vel= 100 % PDAT1 Tool[0] Base[0];%{PE}%R 5.2.22,%MKUKATPBASIS,%CMOVE,%VPTP,%P 1:PTP, 2:PPI1, 3:, 5:100, 7:PDAT1${nl}  $$BWDSTART = FALSE${nl}  PDAT_ACT = PPDAT1${nl}  FDAT_ACT = FPPI1${nl}  BAS(#PTP_PARAMS,100)${nl}  PTP XPPI1${nl};ENDFOLD${nl} ${nl} ${nl};*** Auto Load the Correct TOOL and OFFSET${nl}  ToolChange($tool,1)${nl} ${nl} ${nl};*** Make Initial Position Move ${nl};FOLD PTP PPI1  Vel= 100 % PDAT1 Tool[0] Base[0];%{PE}%R 5.2.22,%MKUKATPBASIS,%CMOVE,%VPTP,%P 1:PTP, 2:PPI1, 3:, 5:100, 7:PDAT1${nl}  $$BWDSTART = FALSE${nl}  PDAT_ACT = PPDAT1${nl}  FDAT_ACT = FPPI1${nl}  BAS(#PTP_PARAMS,100)${nl}  PTP XPPI1${nl};ENDFOLD${nl} ${nl};*** Auto Load The Correct TOOL Data${nl}  $$tool=tool_data[${tool}]${nl} ${nl};*** Auto Load The Correct BASE Data${nl}  $$base=base_data[1]${nl} ${nl}  $$ADVANCE = 5${nl} ${nl}SetRPM($rpm)"
  def toolPathTop(num: String) = s"&ACCESS RVP${nl}&REL 2${nl}&PARAM TEMPLATE = C:\\KRC\\Roboter\\Template\\vorgabe${nl}&PARAM EDITMASK = *${nl}DEF ${toolpathName}_Kuka_APT2${num}T02S${num}()${nl} ${nl} ${nl};FOLD INI;%{PE}%V3.2.0,%MKUKATPBASIS,%CINIT,%VCOMMON,%P${nl}  ;FOLD BAS INI;%{E}%V3.2.0,%MKUKATPBASIS,%CINIT,%VINIT,%P${nl}    GLOBAL INTERRUPT DECL 3 WHEN $$STOPMESS==TRUE DO IR_STOPM ( )${nl}    INTERRUPT ON 3${nl}    BAS (#INITMOV,0 )${nl}  ;ENDFOLD (BAS INI)${nl}  ;FOLD USER INI;%{E}%V3.2.0,%MKUKATPUSER,%CINIT,%VINIT,%P${nl}    ;Make your modifications here${nl}  ;ENDFOLD (USER INI)${nl};ENDFOLD (INI)${nl} ${nl} ${nl};*** Make Null PTP Move ${nl}  PTP $$pos_act${nl} ${nl};*** Auto Load The Correct TOOL Data${nl}  $$tool=tool_data[${tool}]${nl} ${nl};*** Auto Load The Correct BASE Data${nl}  $$base=base_data[1]${nl} ${nl}  $$ADVANCE = 5"

  val groupings: mutable.LinkedHashMap[Int, ListBuffer[(File, String, Boolean)]] ={
    val groupingz = new mutable.LinkedHashMap[Int, ListBuffer[(File, String, Boolean)]]()
    var groupNum = 0
    var total = 0
    var iteration = 1
    files.foreach{f=>
      total += 1
      println(f._2._1.getName)
      if(iteration <= 7 && f._1 != 0) groupingz.update(groupNum, groupingz.getOrElse(groupNum, new ListBuffer[(File, String, Boolean)]) += ((f._2._1, f._2._2, false)))
      else if (f._1 != 0){
        groupingz.update(groupNum, groupingz.getOrElse(groupNum, new ListBuffer[(File, String, Boolean)]) += ((f._2._1, f._2._2, true)))
        iteration = 0
        groupNum += 1
        println(groupNum)
      } else {
        iteration = 0
        groupNum += 1
      }
      iteration += 1
    }
    groupingz
  }

  def writeOut(): Unit ={
    groupings.foreach{g=>
      ui.Console.writeln("------")
      ui.Console.writeln("Writing Group " + g._1.toString)
      ui.Console.writeln("------")
      val directory = if(!Files.isDirectory(Paths.get(path+"/"+g._1.toString+"/")))Files.createDirectory(Paths.get(path+"/"+g._1.toString+"/")) else Paths.get(path+"/"+g._1.toString+"/")
      ui.Console.writeln(directory.toString)
      val mainFileName = files.getOrElse(0, (new File("null"), ""))._1.getName
      ui.Console.writeln("Writing Main-File: " + mainFileName)
      val linker = new PrintWriter(new File(directory+"/"+mainFileName))
      linker.println(mainTop)
      g._2.foreach(f=>linker.println(s"EXT ${f._1.getName.replace(".SRC", "()")}"))
      if(g._1 == 1) linker.println(goHomeAndInit)
      g._2.foreach(f=>linker.println(s"${f._1.getName.replace(".SRC", "()")}"))
      linker.println(mainBottom)
      linker.close()
      ui.Console.writeln("Wrote Main-File: " + mainFileName)
      g._2.foreach{f=>
        val writer = new PrintWriter(new File(directory+"/"+f._1.getName))
        ui.Console.writeln("Getting middle of " + f._1.getName)
        val middle = Source.fromFile(f._1).getLines().mkString(nl).split(s"SetRPM\\($rpm\\)")(1).split("END")(0)
        ui.Console.writeln("Writing " + f._1.getName)
        if(f._2 == "1") writer.println(toolPathFirstTop(f._2))
        else writer.println(toolPathTop(f._2))
        if(f._2 != "1") writer.println(s"MPM(30000)$nl" + middle)
        if(f._3)writer.println("SetRPM(0)")
        writer.println("END")
        writer.close()
        ui.Console.writeln("Wrote Sub-File: " + directory+"/"+f._1.getName)
        ui.Console.writeln("------")
      }
      ui.Console.writeln("Group " + g._1.toString + " written")
    }
  }

}
