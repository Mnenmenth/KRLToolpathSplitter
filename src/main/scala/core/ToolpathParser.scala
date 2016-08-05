package core

import java.nio.file.Paths

import ui.MainWindow

import scalafx.application.JFXApp

/**
  * Created by Mnenmenth Alkaborin
  * Please refer to LICENSE file if included
  * for licensing information
  * https://github.com/Mnenmenth
  */
object ToolpathParser extends JFXApp {

  val window = new MainWindow
  stage = window

  override def stopApp(): Unit ={
    sys.exit(0)
  }

}
