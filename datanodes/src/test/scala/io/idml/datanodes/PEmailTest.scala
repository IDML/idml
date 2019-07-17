package io.idml.datanodes

import javax.mail.internet.InternetAddress

import io.idml.{CastFailed, IdmlNull, NoFields}
import org.scalatest._

/** Test the behaviour of the PEmail class */
class PEmailTest extends FunSuite with MustMatchers {

  test("Empty strings cannot be turned into emails") {
    PString("").email() must equal(CastFailed)
  }

  test("Parse a plain email address") {
    PString("andi.miller@datasift.com").email() must equal(new PEmail(new InternetAddress("andi.miller@datasift.com")))
  }

  test("Parse an email address wtih a name") {
    PString("Andi Miller <andi.miller@datasift.com>").email() must equal(
      new PEmail(new InternetAddress("Andi Miller <andi.miller@datasift.com>")))
  }

  test("Apply email() to an empty array") {
    PArray().email() must equal(PArray())
  }

  test("Apply email() to an array with a single address") {
    PArray(PString("andi.miller@datasift.com")).email() must equal(PArray(new PEmail(new InternetAddress("andi.miller@datasift.com"))))
  }

  test("Apply email() to an array with an invalid address") {
    PArray(
      PString("THISISNOTANEMAILADDRESS"),
      PString("andi.miller@datasift.com")
    ).email() must equal(
      PArray(
        CastFailed,
        new PEmail(new InternetAddress("andi.miller@datasift.com"))
      ))
  }

  test("Parse an array with multiple emails") {
    PArray(
      PString("andi.miller@datasift.com"),
      PString("bob@gmail.com"),
      PString("firstname_lastname@company-name.co.uk")
    ).email() must equal(
      PArray(
        new PEmail(new InternetAddress("andi.miller@datasift.com")),
        new PEmail(new InternetAddress("bob@gmail.com")),
        new PEmail(new InternetAddress("firstname_lastname@company-name.co.uk"))
      )
    )
  }

  test("Get fields with an invalid address") {
    val email = PString("").email()
    email.get("address") must equal(NoFields)
    email.get("name") must equal(NoFields)
    email.get("domain") must equal(NoFields)
    email.get("username") must equal(NoFields)
  }

  test("Get fields from a simple email address") {
    val email = PString("andi.miller@datasift.com").email()
    email.get("address") must equal(PString("andi.miller@datasift.com"))
    email.get("name") must equal(IdmlNull)
    email.get("domain") must equal(PString("datasift.com"))
    email.get("username") must equal(PString("andi.miller"))
  }

  test("Get fields from a named email address") {
    val email = PString("Andi Miller <andi.miller@datasift.com>").email()
    email.get("address") must equal(PString("andi.miller@datasift.com"))
    email.get("name") must equal(PString("Andi Miller"))
    email.get("domain") must equal(PString("datasift.com"))
    email.get("username") must equal(PString("andi.miller"))
  }

  test("plus") {
    val e = PString("Andi Miller <andi.miller@datasift.com>").email()
    val s = PString("email: ")
    (s + e) must equal(PString("email: Andi Miller <andi.miller@datasift.com>"))
  }

}
