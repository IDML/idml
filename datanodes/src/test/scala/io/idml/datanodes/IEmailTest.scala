package io.idml.datanodes

import javax.mail.internet.InternetAddress

import io.idml.{CastFailed, IdmlNull, NoFields}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

/** Test the behaviour of the PEmail class */
class IEmailTest extends AnyFunSuite with Matchers {

  test("Empty strings cannot be turned into emails") {
    IString("").email() must equal(CastFailed)
  }

  test("Parse a plain email address") {
    IString("andi.miller@datasift.com").email() must equal(new IEmail(new InternetAddress("andi.miller@datasift.com")))
  }

  test("Parse an email address wtih a name") {
    IString("Andi Miller <andi.miller@datasift.com>").email() must equal(
      new IEmail(new InternetAddress("Andi Miller <andi.miller@datasift.com>")))
  }

  test("Apply email() to an empty array") {
    IArray().email() must equal(IArray())
  }

  test("Apply email() to an array with a single address") {
    IArray(IString("andi.miller@datasift.com")).email() must equal(IArray(new IEmail(new InternetAddress("andi.miller@datasift.com"))))
  }

  test("Apply email() to an array with an invalid address") {
    IArray(
      IString("THISISNOTANEMAILADDRESS"),
      IString("andi.miller@datasift.com")
    ).email() must equal(
      IArray(
        CastFailed,
        new IEmail(new InternetAddress("andi.miller@datasift.com"))
      ))
  }

  test("Parse an array with multiple emails") {
    IArray(
      IString("andi.miller@datasift.com"),
      IString("bob@gmail.com"),
      IString("firstname_lastname@company-name.co.uk")
    ).email() must equal(
      IArray(
        new IEmail(new InternetAddress("andi.miller@datasift.com")),
        new IEmail(new InternetAddress("bob@gmail.com")),
        new IEmail(new InternetAddress("firstname_lastname@company-name.co.uk"))
      )
    )
  }

  test("Get fields with an invalid address") {
    val email = IString("").email()
    email.get("address") must equal(NoFields)
    email.get("name") must equal(NoFields)
    email.get("domain") must equal(NoFields)
    email.get("username") must equal(NoFields)
  }

  test("Get fields from a simple email address") {
    val email = IString("andi.miller@datasift.com").email()
    email.get("address") must equal(IString("andi.miller@datasift.com"))
    email.get("name") must equal(IdmlNull)
    email.get("domain") must equal(IString("datasift.com"))
    email.get("username") must equal(IString("andi.miller"))
  }

  test("Get fields from a named email address") {
    val email = IString("Andi Miller <andi.miller@datasift.com>").email()
    email.get("address") must equal(IString("andi.miller@datasift.com"))
    email.get("name") must equal(IString("Andi Miller"))
    email.get("domain") must equal(IString("datasift.com"))
    email.get("username") must equal(IString("andi.miller"))
  }

  test("plus") {
    val e = IString("Andi Miller <andi.miller@datasift.com>").email()
    val s = IString("email: ")
    (s + e) must equal(IString("email: Andi Miller <andi.miller@datasift.com>"))
  }

}
