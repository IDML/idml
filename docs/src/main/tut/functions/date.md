---
layout: docsplus
title: Date
section: language
---

The Date functions allow you to manipulate the reading and writing of dates.

### Quick Reference

<table class="table table-condensed">
  <tbody>
    <tr>
      <td></td>
      <th colspan="5" class="success text-center">Desired Output</th>
    </tr>
    <tr>
      <th class="info">Input</th>
      <th class="success">epoch</th>
      <th class="success">epoch millis</th>
      <th class="success">rfc822</th>
      <th class="success">iso8601</th>
      <th class="success">custom</th>
    </tr>
    <tr>
      <th class="info">epoch</th>
      <td>-</td>
      <td>.date().toEpochMillis()</td>
      <td>.date()</td>
      <td>.date().date($op)</td>
      <td>.date().date($op)</td>
    </tr>
    <tr>
      <th class="info">epoch millis</th>
      <td>.millis().toEpoch()</td>
      <td>-</td>
      <td>.millis()</td>
      <td>.millis().date($op)</td>
      <td>.millis().date($op)</td>
    </tr>
    <tr>
      <th class="info">rfc822</th>
      <td>.date().toEpoch()</td>
      <td>.date().toEpochMillis()</td>
      <td>-</td>
      <td>.date().date($op)</td>
      <td>.date().date($op)</td>
    </tr>
    <tr>
      <th class="info">iso8601</th>
      <td>.date().toEpoch()</td>
      <td>.date().toEpochMillis()</td>
      <td>.date()</td>
      <td>-</td>
      <td>.date().date($op)</td>
    </tr>
    <tr>
      <th class="info">custom</th>
      <td>.date($ip).toEpoch()</td>
      <td>.date($ip).toEpochMillis()</td>
      <td>.date($ip)</td>
      <td>.date($ip).date($op)</td>
      <td>.date($ip).date($op)</td>
    </tr>
    <tr>
      <th class="info">no input</th>
      <td>now().toEpoch()</td>
      <td>now().toEpochMillis()</td>
      <td>now()</td>
      <td>now().date($op)</td>
      <td>now().date($op)</td>
    </tr>
  </tbody>
</table>

This table assumes `let ip = "..."` for the input pattern and `let op = "..."` for the output pattern.

For iso8601 the output pattern is `"yyyy-MM-dd'T'HH:mm:ss.SSSZZ"`.

Patterns must be joda-time compatible, please refer to [this documentation](https://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html).

## date

The date function attempts to transform a value into a date object that will be rendered in RFC 822 format.

	 output = input.date()

It can also be called with a string argument which is a [joda-time compatible pattern](https://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html). When called like this on a string, it will parse using that pattern, and when called on a date, it will output the date in that format.

```
output = "2018-03-15T14:37:42.289+00:00".date("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")     # parse using a pattern
output = now().date("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")                               # format using a pattern
```

### Validating a date

The simplest usage of this function is to validate a date. This will parse the date and if it is valid, format it in the RFC 822 style.

	 {"input": "Tue, 20 May 2014 11:22:34 +0000"}
	 {"output": "Tue, 20 May 2014 11:22:34 +0000"}

#### A date that is missing a day value

	 {"input": "Tue 2014 11:22:34 +0000"}
	 {}

#### A date with an invalid day value
	 {"input": "Tue, 33 May 2014 11:22:34 +0000"}
	 {}

#### Timezones

Dates that contain timezones will not be automatically converted to UTC

	 {"input": "Tue, 20 May 2014 11:22:34 +0500"}
	 {"output": "Tue, 20 May 2014 11:22:34 +0500"}

### Parsing unix timestamps
Numbers are automatically assumed to be unix timestamps and converted accordingly. Timestamps are always assumed to be in the UTC timezone

	 {"input": 1400581354}
	 {"output": "Tue, 20 May 2014 10:22:34 +0000"}

### Supported string formats

#### RFC 822
This corresponds to `EEE, dd MMM yyyy HH:mm:ss Z` the style originally created for SMTP.

It is widely used in emails, SMTP and is also the style used by DataSift to render dates as json strings.

	 {"input": "Tue, 20 May 2014 11:22:34 +0000"}
	 {"output": "Tue, 20 May 2014 11:22:34 +0000"}

#### ISO 8601 date and time with timezone
This corresponds to the Java date format string `yyyy-MM-dd'T'HH:mm:ssZZ`

	 {"input": "2014-05-20T11:22:34+00:00"}
	 {"output": "Tue, 20 May 2014 11:22:34 +0000"}

#### ISO 8601 date and time without timezone
This corresponds to the Java date format string `yyyy-MM-dd'T'HH:mm:ss`. It will be assumed that the timezone is `+0000`

	 {"input": "2014-05-20T11:22:34"}
	 {"output": "Tue, 20 May 2014 11:22:34 +0000"}

#### ISO date without timezone
This corresponds to the Java date format string `yyyy-MM-dd`. It will be assumed that the timezone is `+0000` and the time will default to  `00:00:00`.

	 {"input": "2014-05-20"}
	 {"output": "Tue, 20 May 2014 00:00:00 +0000"}

#### ISO8601 datetime without T

	 {"input": "2014-05-20 11:22:34"}
	 {"output": "Tue, 20 May 2014 11:22:34 +0000"}

### Unsupported values

Types of data and string formats not described above are not supported. As demonstrated:

	 {"input": false}
	 {}
	 {"input": "hello world"}
	 {}

### Formatted dates

	 formattedOutput = formattedInput.date("yyyy.MM.dd G 'at' HH:mm:ss z")

#### Custom date format
Any custom formatted date is correctly parsed and translated to GMT

	 {"formattedInput": "2001.07.04 AD at 12:08:56 PDT"}
	 {"formattedOutput": "Wed, 04 Jul 2001 12:08:56 -0700"}

## millis

This is called on an integer, and parses it as a millisecond timestamp

```
{"millisInput": 1413803342198}
millisOutput = millisInput.millis()
{"millisOutput" : "Mon, 20 Oct 2014 11:09:02 +0000"}
```

## now

This is called on it's own and returns a Date object for the current time

```
timestamp = now()
```

## microtime

This is called on it's own and returns the current microsecond timestamp as an integer

```
timestamp = microtime()
```

## timezone

This is called on a Date and is used to apply a timezone to that date

```
timestamp = now().timezone("PST")   # gives you "Thu, 15 Mar 2018 07:44:55 -0700"
```



