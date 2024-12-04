package com.google.firebase.dataconnect.minimaldemo

import com.google.firebase.dataconnect.LocalDate
import com.google.firebase.dataconnect.OptionalVariable
import com.google.firebase.dataconnect.minimaldemo.connector.InsertItemMutation
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.Sample
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.next
import io.kotest.property.arbs.fooddrink.iceCreamFlavors
import io.kotest.property.asSample
import java.time.Month

enum class Optionality {
  Null,
  NonNull,
  Absent,
}

fun <T> Arb.Companion.optionalVariable(
  arb: Arb<T>,
  optionality: Arb<Optionality> = Arb.enum<Optionality>(),
): Arb<OptionalVariable<T?>> = arbitrary {
  when (optionality.bind()) {
    Optionality.Absent -> OptionalVariable.Undefined
    Optionality.Null -> OptionalVariable.Value(null)
    Optionality.NonNull -> OptionalVariable.Value(arb.bind())
  }
}

fun Arb.Companion.insertItemVariables(): Arb<InsertItemMutation.Variables> =
  InsertItemMutationVariablesArb()

private class InsertItemMutationVariablesArb(
  private val string: Arb<String?> = Arb.iceCreamFlavors().map { it.value },
  private val int: Arb<Int> = Arb.int(),
  private val int64: Arb<Long> = Arb.long(),
  private val float: Arb<Double> = Arb.double().filterNot { it.isNaN() || it.isInfinite() },
  private val boolean: Arb<Boolean> = Arb.boolean(),
  private val date: Arb<LocalDate> = Arb.dataConnectLocalDate(),
) : Arb<InsertItemMutation.Variables>() {
  override fun edgecase(rs: RandomSource): InsertItemMutation.Variables =
    InsertItemMutation.Variables(
      string = string.optionalEdgeCase(rs),
      int = int.optionalEdgeCase(rs),
      int64 = int64.optionalEdgeCase(rs),
      float = float.optionalEdgeCase(rs),
      boolean = boolean.optionalEdgeCase(rs),
      date = date.optionalEdgeCase(rs),
      timestamp = OptionalVariable.Undefined,
      any = OptionalVariable.Undefined,
    )

  override fun sample(rs: RandomSource): Sample<InsertItemMutation.Variables> =
    InsertItemMutation.Variables(
        string = OptionalVariable.Value(string.next(rs)),
        int = OptionalVariable.Value(int.next(rs)),
        int64 = OptionalVariable.Value(int64.next(rs)),
        float = OptionalVariable.Value(float.next(rs)),
        boolean = OptionalVariable.Value(boolean.next(rs)),
        date = OptionalVariable.Value(date.next(rs)),
        timestamp = OptionalVariable.Undefined,
        any = OptionalVariable.Undefined,
      )
      .asSample()
}

fun Arb.Companion.dataConnectLocalDate(): Arb<LocalDate> = DataConnectLocalDateArb()

private class DataConnectLocalDateArb : Arb<LocalDate>() {

  private val yearArb = Arb.int(MIN_YEAR..MAX_YEAR)
  private val monthArb = Arb.enum<Month>()
  private val dayArbByMonthLength = mutableMapOf<Int, Arb<Int>>()

  override fun edgecase(rs: RandomSource): LocalDate {
    val year = yearArb.maybeEdgeCase(rs, edgeCaseProbability = 0.33f)
    val month = monthArb.maybeEdgeCase(rs, edgeCaseProbability = 0.33f)
    val day = dayArbFor(month, year).maybeEdgeCase(rs, edgeCaseProbability = 0.33f)
    return LocalDate(year = year, month = month.value, day = day)
  }

  override fun sample(rs: RandomSource): Sample<LocalDate> {
    val year = yearArb.sample(rs).value
    val month = monthArb.sample(rs).value
    val day = dayArbFor(month, year).sample(rs).value
    return LocalDate(year = year, month = month.value, day = day).asSample()
  }

  private fun dayArbFor(month: Month, year: Int): Arb<Int> {
    val monthLength = java.time.Year.of(year).atMonth(month).lengthOfMonth()
    return dayArbByMonthLength.getOrPut(monthLength) { Arb.int(0..monthLength) }
  }

  companion object {
    const val MIN_YEAR = 1583
    const val MAX_YEAR = 9999
  }
}

private fun <T> Arb<T>.optionalEdgeCase(rs: RandomSource): OptionalVariable<T?> {
  val discriminator = rs.random.nextFloat()
  return if (discriminator < 0.25f) {
    OptionalVariable.Undefined
  } else if (discriminator < 0.50f) {
    OptionalVariable.Value(null)
  } else {
    OptionalVariable.Value(edgecase(rs) ?: next(rs))
  }
}

private fun <T> Arb<T>.maybeEdgeCase(rs: RandomSource, edgeCaseProbability: Float = 0.5f): T {
  require(edgeCaseProbability >= 0.0 && edgeCaseProbability < 1.0) {
    "invalid edgeCaseProbability: $edgeCaseProbability"
  }
  return if (rs.random.nextFloat() >= edgeCaseProbability) {
    sample(rs).value
  } else {
    edgecase(rs) ?: sample(rs).value
  }
}
