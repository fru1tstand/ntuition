package me.fru1t.ntuition

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.WindowEvent
import me.fru1t.ntuition.controller.MainController
import me.fru1t.ntuition.controller.StatsController
import me.fru1t.ntuition.data.Hiragana
import me.fru1t.ntuition.data.Katakana

fun main(args: Array<String>) {
  Application.launch(NtuitionApplication::class.java, *args)
}

class NtuitionApplication : Application() {
  private lateinit var mainController: MainController
  private var statsStage: Stage? = null
  private lateinit var statsController: StatsController

  inner class WordSetSwapper {
    fun hiragana() {
      initWith(Hiragana.words)
    }

    fun katakana() {
      initWith(Katakana.words)
    }

    fun inverseHiragana() {
      val inverse = Hiragana.words.entries.associateBy({ it.value }) { it.key }
      initWith(inverse)
    }

    fun inverseKatakana() {
      val inverse = Katakana.words.entries.associateBy({ it.value }) { it.key }
      initWith(inverse)
    }

    private fun initWith(words: Map<String, String>) {
      reCreateStatsStage()
      mainController.setWords(words.entries)
      statsController.setWords(words.keys)
    }
  }
  private val wordSetSwapper = WordSetSwapper()

  override fun start(primaryStage: Stage?) {
    val mainUrl = NtuitionApplication::class.java.getResource("/fxml/MainWindow.fxml")
    val mainLoader = FXMLLoader(mainUrl)
    val mainRoot: Parent = mainLoader.load()
    mainController = mainLoader.getController()
    primaryStage!!.scene = Scene(mainRoot)
    mainController.setWordSetSwapper(wordSetSwapper)
    primaryStage.show()
    mainController.onShow()

    // Seed with katakana
    wordSetSwapper.katakana()

    // Close everything if the main window is closed
    primaryStage.scene.window.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST) {
      primaryStage.close()
      statsStage?.close()
    }
  }

  private fun reCreateStatsStage() {
    var oldXPos: Double? = null
    var oldYPos: Double? = null
    statsStage?.let {
      oldXPos = it.x
      oldYPos = it.y
      it.close()
    }

    statsStage = Stage()

    if (oldXPos != null && oldYPos != null) {
      statsStage!!.x = oldXPos!!
      statsStage!!.y = oldYPos!!
    }

    val statsWindowUrl = NtuitionApplication::class.java.getResource("/fxml/StatsWindow.fxml")
    val statsLoader = FXMLLoader(statsWindowUrl)
    val statsRoot: Parent = statsLoader.load()
    statsController = statsLoader.getController()
    statsStage!!.scene = Scene(statsRoot)
    statsStage!!.show()
    statsController.onShow()

    // Hook up listeners
    mainController.setOnSuccessListener(statsController::onSuccessfulInput)
  }
}