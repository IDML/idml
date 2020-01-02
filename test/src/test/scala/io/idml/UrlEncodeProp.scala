package io.idml

import io.idml.datanodes.IString
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties

class UrlEncodeProp extends Properties("UrlEncode") {

  property("urlencode should be reversible") = forAll { s: String =>
    IString(s).urlEncode().urlDecode() == IString(s)
  }
}
