package me.fru1t.ntuition.controller

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.TextField
import me.fru1t.me.fru1t.ntuition.collect.DistributionSet
import me.fru1t.ntuition.NtuitionApplication

class MainController {
  @FXML private lateinit var textIn: TextField
  @FXML private lateinit var prompt: Label
  @FXML private lateinit var pastPrompt: Label

  private var onSuccessListener: ((String, Long) -> Unit)? = null
  private var promptDistribution: DistributionSet<Map.Entry<String, String>> = DistributionSet(setOf())
  private var currentQueue: Array<Map.Entry<String, String>> = arrayOf()
  private val pastQueue = Array(5) { "" }
  private var lastSuccessTime: Long = 0
  private lateinit var wordSetSwapper: NtuitionApplication.WordSetSwapper

  fun onShow() {
    redraw()
    textIn.textProperty().addListener { _, _, _ -> onTextChange() }
  }

  fun setOnSuccessListener(listener: (String, Long) -> Unit) {
    onSuccessListener = listener
  }

  fun setWordSetSwapper(wordSetSwapper: NtuitionApplication.WordSetSwapper) {
    this.wordSetSwapper = wordSetSwapper
  }

  fun setWords(words: Set<Map.Entry<String, String>>) {
    promptDistribution = DistributionSet(words)
    currentQueue = Array(5) { promptDistribution.getNext() }
    pastQueue.forEachIndexed { index, _ -> pastQueue[index] = "" }
    redraw()
  }

  private fun onTextChange() {
    println("key in: " + textIn.text)
    if (textIn.text == "!") {
      lastSuccessTime = 0
      Platform.runLater { textIn.text = "" }
      return
    }
    if (textIn.text == currentQueue[0].value) {
      notifySuccessListeners(currentQueue[0].key)
      nextWord()
      Platform.runLater { textIn.text = "" }
    }
  }

  private fun notifySuccessListeners(key: String) {
    val successTime = System.currentTimeMillis()
    val delta = successTime - lastSuccessTime
    lastSuccessTime = successTime
    if (delta > 20000) {
      println("More than 5 seconds between text, throwing away this input.")
      return
    }
    onSuccessListener?.invoke(key, delta)
  }

  private fun nextWord() {
    val finishedWord = currentQueue[0]
    for (i in 0 until currentQueue.size - 1) {
      pastQueue[i] = pastQueue[i + 1]
      currentQueue[i] = currentQueue[i + 1]
    }
    currentQueue[currentQueue.size - 1] = promptDistribution.getNext()
    pastQueue[pastQueue.size - 1] = finishedWord.key
    redraw()
  }

  private fun redraw() {
    prompt.text = currentQueue.joinToString(separator = " ") { it.key }
    pastPrompt.text = pastQueue.joinToString(separator = " ", postfix = " ") { it }
  }

  @FXML
  private fun onHiraganaAction() {
    wordSetSwapper.hiragana()
  }

  @FXML
  private fun onKatakanaAction() {
    wordSetSwapper.katakana()
  }
}