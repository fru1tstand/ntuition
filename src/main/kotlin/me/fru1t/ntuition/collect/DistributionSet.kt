package me.fru1t.me.fru1t.ntuition.collect

import java.lang.RuntimeException
import java.util.Random

class DistributionSet<T>(private val entries: Set<T>) : Set<T> {
  private companion object {
    private const val MINIMUM_FETCHES_BEFORE_RESELECTION_POSSIBLE = 5
  }

  /**
   * Metadata associated to a single entry that quantifies the probability it will be selected
   * in the next fetch to this set.
   */
  private data class DistributionMetadata(
      var currentWeight: Int = 100,
      var lastChosen: Int = -1,
      var overallWeight: Double = 1.0)

  // Set-interface overrides
  override val size: Int = entries.size
  override fun contains(element: T): Boolean = entries.contains(element)
  override fun containsAll(elements: Collection<T>): Boolean = entries.containsAll(entries)
  override fun isEmpty(): Boolean = entries.isEmpty()
  override fun iterator(): Iterator<T> = entries.iterator()

  private val random: Random = Random()
  private val distributionMetadata: Map<T, DistributionMetadata> = entries.associate { Pair(it, DistributionMetadata()) }
  private var lastChosen: Int = 0

  private fun Map<T, DistributionMetadata>.getOrError(entry: T): DistributionMetadata =
      this[entry] ?: error("Entry was not found. wtf did you do.")

  /** Returns a value from this set following current distribution state, and updates the state. */
  fun getNext(): T {
    var totalWeights = 0
    distributionMetadata.values.forEach { totalWeights += it.currentWeight }

    val randomValue = random.nextInt(totalWeights)

    var cumulativeWeights = 0
    var selectedEntry: T? = null
    for (it in distributionMetadata.entries) {
      cumulativeWeights += it.value.currentWeight
      if (cumulativeWeights > randomValue) {
        selectedEntry = it.key
        break
      }
    }
    if (selectedEntry == null) {
      throw RuntimeException(
          "An entry was not selected from the distributionMetadata. There is an implementation error.")
    }

    // Update distribution
    rebalanceDistribution(selectedEntry)

    return selectedEntry
  }

  fun setWeight(entry: T, weight: Double) {
    distributionMetadata.getOrError(entry).overallWeight = weight
  }

  fun resetWeight(entry: T) {
    distributionMetadata.getOrError(entry).overallWeight = 1.0
  }

  fun getWeight(entry: T): Double {
    return distributionMetadata.getOrError(entry).overallWeight
  }

  override fun toString(): String {
    val builder = StringBuilder()
    distributionMetadata.forEach {
      builder.append(it.key.toString())
          .append(" (")
          .append(it.value.currentWeight)
          .append("), ")
    }
    return builder.toString()
  }

  private fun rebalanceDistribution(selectedEntry: T) {
    lastChosen++
    val selectedEntryData =
        distributionMetadata[selectedEntry] ?: error("Entry was not found. wtf did you do.")
    selectedEntryData.lastChosen = lastChosen
    selectedEntryData.currentWeight = 0
    distributionMetadata.values.forEach {
      if (it.lastChosen <= lastChosen - MINIMUM_FETCHES_BEFORE_RESELECTION_POSSIBLE) {
        it.currentWeight += (100 * it.overallWeight).toInt()
      }
    }
  }
}