/*
 * Copyright 2001-2014 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatest

/**
 * Enables testing of asynchronous code without blocking,
 * using a style consistent with traditional <code>FunSuite</code> tests.
 *
 * <p>
 * Given a <code>Future</code> returned by the code you are testing,
 * you need not block until the <code>Future</code> completes before
 * performing assertions against its value. You can instead map those
 * assertions onto the <code>Future</code> and return the resulting
 * <code>Future[Assertion]</code> to ScalaTest. The test will complete
 * asynchronously, when the <code>Future[Assertion]</code> completes.
 *
 * Here's an example <code>AsyncFunSuite</code>:
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.asyncfunsuite

 * import org.scalatest.AsyncFunSuite
 * import scala.concurrent.Future
 * import scala.concurrent.ExecutionContext
 *
 * class AddSuite extends AsyncFunSuite {
 *
 *   implicit val executionContext = ExecutionContext.Implicits.global
 *
 *   def addSoon(addends: Int*): Future[Int] = Future { addends.sum }
 *
 *   test("addSoon will eventually compute a sum of passed Ints") {
 *     val futureSum: Future[Int] = addSoon(1, 2)
 *     // You can map assertions onto a Future, then return
 *     // the resulting Future[Assertion] to ScalaTest:
 *     futureSum map { sum =&gt; assert(sum == 3) }
 *   } 
 *
 *   def addNow(addends: Int*): Int = addends.sum
 *
 *   test("addNow will immediately compute a sum of passed Ints") {
 *     val sum: Int = addNow(1, 2)
 *     // You can also write synchronous tests, which
 *     // must result in type Assertion:
 *     assert(sum == 3)
 *   }
 * }
 * </pre>
 *
 * <p>
 * &ldquo;<code>test</code>&rdquo; is a method, defined in <code>AsyncFunSuite</code>, which will be invoked
 * by the primary constructor of <code>AddSuite</code>. You specify the name of the test as
 * a string between the parentheses, and the test code itself between curly braces.
 * The test code is a function passed as a by-name parameter to <code>test</code>, which registers
 * it for later execution. The result type of the by-name in an <code>AsyncFunSuite</code> must
 * be <code>Future[Assertion]</code>. 
 * </p>
 *
 * <p>
 * <code>AsyncFunSuite</code> allows you to test asynchronous code without blocking. Instead of using
 * <code>scala.concurrent.Await</code> or <code>org.scalatest.concurrent.ScalaFutures</code> to 
 * block until a <code>Future</code> completes, then performing assertions on the result of the
 * <code>Future</code>, you map the assertions directly onto the <code>Future</code>. ScalaTest
 * assertions and matchers have result type <code>Assertion</code>. Thus the result type of the
 * first test in the example above is <code>Future[Assertion]</code>. For clarity, here's the relevant code
 * in a REPL session:
 * </p>
 *
 * <pre class="stREPL">
 * scala&gt; import org.scalatest._
 * import org.scalatest._
 *
 * scala&gt; import Assertions._
 * import Assertions._
 *
 * scala&gt; import scala.concurrent.Future
 * import scala.concurrent.Future
 *
 * scala&gt; import scala.concurrent.ExecutionContext
 * import scala.concurrent.ExecutionContext
 *
 * scala&gt; implicit val executionContext = ExecutionContext.Implicits.global
 * executionContext: scala.concurrent.ExecutionContextExecutor = scala.concurrent.impl.ExecutionContextImpl@26141c5b
 *
 * scala&gt; def addSoon(addends: Int*): Future[Int] = Future { addends.sum }
 * addSoon: (addends: Int*)scala.concurrent.Future[Int]
 *
 * scala&gt; val futureSum: Future[Int] = addSoon(1, 2)
 * futureSum: scala.concurrent.Future[Int] = scala.concurrent.impl.Promise$DefaultPromise@721f47b2
 *
 * scala&gt; futureSum map { sum =&gt; assert(sum == 3) }
 * res0: scala.concurrent.Future[org.scalatest.Assertion] = scala.concurrent.impl.Promise$DefaultPromise@3955cfcb
 * </pre>
 *
 * <p>
 * The second test has result type <code>Assertion</code>:
 * </p>
 * 
 * <pre class="stREPL">
 * scala&gt; def addNow(addends: Int*): Int = addends.sum
 * addNow: (addends: Int*)Int
 *
 * scala&gt; val sum: Int = addNow(1, 2)
 * sum: Int = 3
 *
 * scala&gt; assert(sum == 3)
 * res1: org.scalatest.Assertion = Succeeded
 * </pre>
 * 
 * <p>
 * The second test will be implicitly converted to <code>Future[Assertion]</code> and registered.
 * The implicit conversion is from <code>Assertion</code> to <code>Future[Assertion]</code>, so
 * you must end synchronous tests in some ScalaTest assertion or matcher expression. If you need to,
 * you can put <code>succeed</code> at the end of the test body. <code>succeed</code> is a field in
 * trait <code>Assertions</code> that returns the <code>Succeeded</code> singleton:
 * </p>
 *
 * <pre class="stREPL">
 * scala&gt; import org.scalatest.Assertions._
 * import org.scalatest.Assertions._
 *
 * scala&gt; succeed
 * res2: org.scalatest.Assertion = Succeeded
 * </pre>
 *
 * <p>
 * Thus placing <code>succeed</code> at the end of a test body will solve
 * the type error:
 * </p>
 *
 * <pre class="stHighlight">
 *   test("addNow will immediately compute a sum of passed Ints") {
 *     val sum: Int = addNow(1, 2)
 *     assert(sum == 3)
 *     println("hi") // println has result type Unit
 *     succeed       // succeed has result type Assertion
 *   }
 * </pre>
 *
 * <p>
 * An <code>AsyncFunSuite</code>'s lifecycle has two phases: the <em>registration</em> phase and the
 * <em>ready</em> phase. It starts in registration phase and enters ready phase the first time
 * <code>run</code> is called on it. It then remains in ready phase for the remainder of its lifetime.
 * </p>
 *
 * <p>
 * Tests can only be registered with the <code>test</code> method while the <code>AsyncFunSuite</code> is
 * in its registration phase. Any attempt to register a test after the <code>AsyncFunSuite</code> has
 * entered its ready phase, <em>i.e.</em>, after <code>run</code> has been invoked on the <code>AsyncFunSuite</code>,
 * will be met with a thrown <code>TestRegistrationClosedException</code>. The recommended style
 * of using <code>AsyncFunSuite</code> is to register tests during object construction as is done in all
 * the examples shown here. If you keep to the recommended style, you should never see a
 * <code>TestRegistrationClosedException</code>.
 * </p>
 *
 */
abstract class AsyncFunSuite extends AsyncFunSuiteLike {

  /**
   * Returns a user friendly string for this suite, composed of the
   * simple name of the class (possibly simplified further by removing dollar signs if added by the Scala interpeter) and, if this suite
   * contains nested suites, the result of invoking <code>toString</code> on each
   * of the nested suites, separated by commas and surrounded by parentheses.
   *
   * @return a user-friendly string for this suite
   */
  override def toString: String = Suite.suiteToString(None, this)
}
