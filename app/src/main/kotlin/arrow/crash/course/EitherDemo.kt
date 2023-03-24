/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package arrow.crash.course

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.flatMap
import arrow.core.left

/** Using either to represent a failure at the type leve */

/**
 * This function either fails (and returns a string wrapped in a left) or succeeds and returns Unit
 * wrapped in a Right
 */
fun foo(): Either<String, Unit> = TODO()

/** This explodes when you try to divide by 0 */
fun makePie(filling: PieFlavor): Either<MakePieFailure, Pie> = MakePieFailure.Burnt.left()

data class Pie(val flavor: PieFlavor)

enum class PieFlavor {
  Pumpkin,
  Apple,
  Cherry,
}

sealed interface MakePieFailure {
  object Burnt : MakePieFailure
  object Raw : MakePieFailure
}

/**
 * Let's compose it with something else
 *
 * Let's assume serving a pie always succeeds
 */
fun servePie(pie: Pie): Unit = TODO()

fun makeAndServeAPie(filling: PieFlavor): Either<MakePieFailure, Unit> =
    makePie(filling).map { pie -> servePie(pie) }

/** What if we need to deliver the pie before serving, and that can also fail? */
sealed interface DeliverPieFailure {
  object GotLost : DeliverPieFailure
  object TookTooLongAndPieWentBad : DeliverPieFailure
  object IsABomb : DeliverPieFailure
  object Oops : DeliverPieFailure
}

fun deliverPie(pie: Pie): Either<DeliverPieFailure, Unit> = TODO()

sealed interface MakeAndDeliverAndServePieFailure {
  data class FailedToMakePie(val failure: MakePieFailure) : MakeAndDeliverAndServePieFailure
  data class FailedToDeliverPie(val failure: DeliverPieFailure) : MakeAndDeliverAndServePieFailure
}

fun makeAndDeliverAndServePie(filling: PieFlavor): Either<MakeAndDeliverAndServePieFailure, Unit> =
    makePie(filling)
        .mapLeft { MakeAndDeliverAndServePieFailure.FailedToMakePie(it) }
        .flatMap { pie ->
          deliverPie(pie)
              .mapLeft { MakeAndDeliverAndServePieFailure.FailedToDeliverPie(it) }
              .map { pie }
        }
        .map { servePie(it) }

/**
 * This deep nesting can get a little much sometimes (though I wouldn't say its too bad in the above
 * example). But in general, if you are composing multiple eithers or fetching multiple different
 * either values that you need to compose after (like fetch 3 diffeent values from different
 * sources, then combine them):
 *
 * it might be nicer to use the `either` block
 */
suspend fun makeAndDeliverAndServePieWithEitherBlock(
    filling: PieFlavor
): Either<MakeAndDeliverAndServePieFailure, Unit> = either {
  val pie = makePie(filling).mapLeft { MakeAndDeliverAndServePieFailure.FailedToMakePie(it) }.bind()
  deliverPie(pie).mapLeft { MakeAndDeliverAndServePieFailure.FailedToDeliverPie(it) }.bind()
  servePie(pie)
}

/**
 * The main behaviors you want to be aware of are
 * 1) Either.map (runs on the right side and re wraps the value in a right)
 * 2) Either.flatMap (runs on the right side and expects you to rewrap the value in Either (so you
 *    can convert a Right into a Left)
 * 3) Either.leftMap (runs on the left side and re wraps the value in a left)
 * 4) Either.traverse - if you have a List<Either<A,B>> and want to "lift" it into an Either<A,
 *    List<B>>, that is the kind of thing traverse is for
 *
 *    traverse :: [m a] -> m [a] Either.traverse :: List<Either<A,B>> -> Either<A, List<B>>
 *
 * This is how you use the code in the "core" of your system. But at some point you have to "unwrap"
 * the either, usually in a controller or some edge and serialize or throw exceptions or something
 *
 * The easiest way to do this is a combination of `Either.fold` and `when`
 */
suspend fun orderPieController(filling: PieFlavor): String =
    makeAndDeliverAndServePie(filling)
        .fold(
            ifLeft = {
              when (it) {
                is MakeAndDeliverAndServePieFailure.FailedToDeliverPie ->
                    when (it.failure) {
                      DeliverPieFailure.GotLost -> TODO()
                      DeliverPieFailure.IsABomb -> TODO()
                      DeliverPieFailure.TookTooLongAndPieWentBad -> TODO()
                      DeliverPieFailure.Oops -> TODO()
                    }
                is MakeAndDeliverAndServePieFailure.FailedToMakePie ->
                    when (it.failure) {
                      MakePieFailure.Burnt -> TODO()
                      MakePieFailure.Raw -> TODO()
                    }
              }
            },
            ifRight = {
              /**
               * our fun returns Unit so there is nothing here, but if it returned Pie, we would
               * have access to it here and could serialize it
               */
              "Success"
            })
