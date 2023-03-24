package arrow.crash.course

import arrow.optics.Lens
import arrow.optics.optics

/**
 * FP likes immutability. I would recommend trying to be immutable wherever possible unless you are
 * specifically dealing with a concept that requires mutability (usually at the edges of the system
 * or because a framework like hibernate demands it)
 */

/** Data classes vs classes */
data class Employee(val id: String, val name: String)

/** To mutate, just do a copy! */
fun Employee.setName(newName: String) = copy(name = newName)

/** But that can get gross with deeply nested immutable structures */
object DeeplyNested {
  @optics
  data class Street(val number: Int, val name: String) {
    companion object
  }
  @optics
  data class Address(val city: String, val street: Street) {
    companion object
  }
  @optics
  data class Company(val name: String, val address: Address) {
    companion object
  }
  @optics
  data class Employee(val name: String, val company: Company) {
    companion object
  }

  val john =
      Employee(
          "John Doe",
          Company(
              "Kategory",
              Address(
                  "Functional city",
                  Street(42, "lambda street"),
              ),
          ),
      )

  /** Not fun */
  fun Employee.setStreet(street: Street): Employee =
      this.copy(company = this.company.copy(address = this.company.address.copy(street = street)))

  /** Arrow provides optics! Which is the FP way to do deeply nested immutable updates */
  /** Define the lens */
  val streetLens: Lens<Employee, Street> = Employee.company.address.street
  fun Employee.easierSetStreet(street: Street): Employee =
      streetLens.modify(this) {
        /** this is a func so you can be more complicated but im just setting the street */
        street
      }
}

/**
 * Also of note: Collection.fold -> comparable to a mutable loop where you gradually update
 * something
 */
fun sumNumbers(nums: List<Int>) = nums.fold(0) { acc, curr -> acc + curr }
