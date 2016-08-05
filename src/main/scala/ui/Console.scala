package ui

import parser.Parser

import scalafx.application.Platform
import scalafx.scene.control.{Label, TextArea}

/**
  * Created by Mnenmenth Alkaborin
  * Please refer to LICENSE file if included
  * for licensing information
  * https://github.com/Mnenmenth
  */
object Console extends TextArea("Program Initialized\n"){

  prefColumnCount = 240
  prefWidth = 760
  wrapText = true
  editable = false

  def clear: Unit = text.value = ""

  def writeln(toWrite: String): Unit = {
    Platform.runLater {
      text = text.value + toWrite + "\n"
      println(toWrite)
      this.positionCaret(text.value.length)
    }
  }

  def write(toWrite: String): Unit = {
    Platform.runLater {
      text = text.value + toWrite
      print(toWrite)
    }
  }
}