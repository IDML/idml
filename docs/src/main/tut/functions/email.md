---
layout: docsplus
title: Email
section: language
---

## email

The email function parses emails and allows you to access their components.

	 output = input.email()

### Convert existing objects into email objects

This function operates on strings

	 {"input": "andi.miller@datasift.com"}
	 {"output": "andi.miller@datasift.com"}

	 {"input": "invalidemailaddress"}
	 {}

### access parts of email addresses

There are accessors to get the name out of an rfc822 email

     output = input.email().name
     {"input": "Andi Miller <andi.miller@datasift.com>"}
     {"output": "Andi Miller"}

There are accessors to get the email address out of an rfc822 email

     output = input.email().address
     {"input": "Andi Miller <andi.miller@datasift.com>"}
     {"output": "andi.miller@datasift.com"}

There are accessors to get the username out of an rfc822 email

     output = input.email().username
     {"input": "Andi Miller <andi.miller@datasift.com>"}
     {"output": "andi.miller"}

There are accessors to get the domain out of an rfc822 email

     output = input.email().domain
     {"input": "Andi Miller <andi.miller@datasift.com>"}
     {"output": "datasift.com"}

### badly formatted emails should be discarded

     output = input.email()

Triangle brackets with no name.

     {"input": "<@>"}
     {}

Keyboard mashing

     {"input": "kjlas;d12£!$£@%£$"}
     {}

No domain

     {"input": "a <a@>"}
     {}

No username

     {"input": "a <@a>"}
     {}

### Possibly erroneous bad emails

Content after closing brace is discarded

     {"input": "a <a@a> a"}
     {"output": "a <a@a>"}
