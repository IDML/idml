package io.idml

import io.idml.datanodes.IString
import org.scalacheck._
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties

class Base64Prop extends Properties("Base64") {

  property("base64 should be reversible") = forAll { s: String =>
    IString(s).base64encode().base64decode() == IString(s)
  }
  property("base64mime should be reversible") = forAll { s: String =>
    IString(s).base64mimeEncode().base64mimeDecode() == IString(s)
  }
  property("base64 should be reversible") = forAll { s: String =>
    IString(s).base64urlsafeEncode().base64urlsafeDecode() == IString(s)
  }
}
