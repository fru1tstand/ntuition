package me.fru1t.ntuition.controller

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import me.fru1t.me.fru1t.ntuition.collect.DistributionSet
import me.fru1t.ntuition.NtuitionApplication

class MainController {
  private companion object {
    fun isSpace(text: String): Boolean {
      return text == " " || text == "　"
    }

    fun isPeriod(text: String): Boolean {
      return text == "." || text == "。"
    }
  }

  @FXML private lateinit var textIn: TextField
  @FXML private lateinit var prompt: Label
  @FXML private lateinit var pastPrompt: Label

  private var onSuccessListener: ((String, Long) -> Unit)? = null
  private var promptDistribution: DistributionSet<Map.Entry<String, String>> = DistributionSet(setOf())
  private var currentQueue: Array<Map.Entry<String, String>> = arrayOf()
  private val pastQueue = Array(5) { "" }
  private var lastSuccessTime: Long = 0
  private lateinit var wordSetSwapper: NtuitionApplication.WordSetSwapper
  private var isInverseMode = false

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
    if (isPeriod(textIn.text)) {
      lastSuccessTime = 0
      Platform.runLater { textIn.text = "" }
      return
    }
    if (textIn.text == currentQueue[0].value || (isInverseMode && isSpace(textIn.text))) {
      notifySuccessListeners(currentQueue[0].key)
      nextWord()
      Platform.runLater { textIn.text = "" }
    }
  }

  private fun notifySuccessListeners(key: String) {
    val successTime = System.currentTimeMillis()
    val delta = successTime - lastSuccessTime
    lastSuccessTime = successTime
    if (delta > 600000) {
      println("More than 1 hour between text, throwing away this input.")
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
    pastQueue[pastQueue.size - 1] = if (isInverseMode) finishedWord.value else finishedWord.key
    redraw()
  }

  private fun redraw() {
    prompt.text = currentQueue.joinToString(separator = " ") { it.key }
    pastPrompt.text = pastQueue.joinToString(separator = " ", postfix = " ") { it }
  }

  @FXML
  private fun onHiraganaAction() {
    wordSetSwapper.hiragana()
    isInverseMode = false
  }

  @FXML
  private fun onKatakanaAction() {
    wordSetSwapper.katakana()
    isInverseMode = false
  }

  @FXML
  private fun onHiraganaClicked(event: MouseEvent) {
    if (event.button == MouseButton.SECONDARY) {
      wordSetSwapper.inverseHiragana()
      isInverseMode = true
    }
  }

  @FXML
  private fun onKatakanaClicked(event: MouseEvent) {
    if (event.button == MouseButton.SECONDARY) {
      wordSetSwapper.inverseKatakana()
      isInverseMode = true
    }
  }
}