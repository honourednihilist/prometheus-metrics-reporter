package com.github.honourednihilist.metrics;

import com.codahale.metrics.MetricRegistry;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import io.restassured.RestAssured;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class PullablePrometheusReporterTest {

	@Test
	void testPullablePrometheusReporter() throws IOException {
		RestAssured.port = SocketUtils.getFreePort();

		MetricRegistry metricRegistry = new MetricRegistry();

		PullablePrometheusReporter metricReporter = PullablePrometheusReporter.forRegistry(metricRegistry)
				.port(RestAssured.port)
				.build();
		metricReporter.start();
		Runtime.getRuntime().addShutdownHook(new Thread(metricReporter::stop));

		metricRegistry.counter(PullablePrometheusReporterTest.class.getName()).inc(42);

		String expected = "# HELP com_github_honourednihilist_metrics_PullablePrometheusReporterTest " +
				"Generated from Dropwizard metric import (metric=com.github.honourednihilist.metrics.PullablePrometheusReporterTest, type=com.codahale.metrics.Counter)\n" +
				"# TYPE com_github_honourednihilist_metrics_PullablePrometheusReporterTest gauge\n" +
				"com_github_honourednihilist_metrics_PullablePrometheusReporterTest 42.0";
		assertThat(RestAssured.get("/metrics").asString(), containsString(expected));
	}
}
