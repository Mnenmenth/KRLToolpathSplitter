package ui

import java.io.File

import parser.{KRLParser, Parser}

import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, TextArea}
import scalafx.scene.layout.{BorderPane, GridPane}
import scalafx.stage.FileChooser
import scalafx.Includes._

/**
  * Created by Mnenmenth Alkaborin
  * Please refer to LICENSE file if included
  * for licensing information
  * https://github.com/Mnenmenth
  */
class WindowContainer extends BorderPane {

  lazy val chooser = new FileChooser

  val chooserButton = new Button("Select Main File"){
    onAction = {
      e: ActionEvent =>
        Console.clear
        chooser.title = "Choose Main File"
        val chosenFile = chooser.showOpenDialog(null)
        if(chosenFile != null) {
          Console.writeln(chosenFile.getAbsolutePath + " chosen")
          if(chosenFile.getName.split('.')(1) == "SRC") {
            //Parser.parse(chosenFile)
            new KRLParser(chosenFile)
          }else{
            Console.writeln("Error! File: " + chosenFile.getName + " is not a toolpath!")
            Console.writeln("Hint: The file should have main in the name and end with .SRC")
          }
        }
    }
  }

  val grid = new GridPane() {

    hgap = 10
    vgap = 10

    add(chooserButton, 0, 0)

    add(Console, 0, 1)

  }



  center = grid

}
