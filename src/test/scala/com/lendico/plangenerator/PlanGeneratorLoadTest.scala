package com.lendico.plangenerator

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.util.Random

class PlanGeneratorLoadTest extends Simulation {

	val httpProtocol = http
		.baseUrl("http://localhost:8080")
		.inferHtmlResources()
		.acceptHeader("*/*")
		.acceptEncodingHeader("gzip, deflate")
  	.contentTypeHeader("application/json;charset=UTF-8")
		.userAgentHeader("PostmanRuntime/7.4.0")

	val rates = Array("1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0", "5.5", "6.0")

	val headers = Map(
		"cache-control" -> "no-cache"
	)

	val scn = scenario("PlanGeneratorLoadTest")
		.pause(1,20)
		.exec(_.set("loanAmount", "" + (1 + Random.nextInt(99999))))
		.exec(_.set("nominalRate", Random.shuffle(rates.toList).head))
		.exec(http("generatePlan")
			.post("/generate-plan")
  		.headers(headers)
			.body(StringBody("""{"loanAmount": "${loanAmount}", "nominalRate": "${nominalRate}", "duration": 24, "startDate": "2018-01-01T00:00:01Z"}"""))
			.check(status.is(200))
		)

	setUp(scn.inject(
		nothingFor(4 seconds),
		atOnceUsers(1000)
	).protocols(httpProtocol)).maxDuration(2 minutes)
}