package com.github.honourednihilist.metrics;

import com.codahale.metrics.MetricRegistry;

import org.junit.jupiter.api.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.mockserver.MockServer;
import org.mockserver.mockserver.MockServerBuilder;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

class PushingPrometheusReporterTest {

	@Test
	void testPushingPrometheusReporter() {
		String host = "localhost";
		int port = SocketUtils.getFreePort();
		String job = PushingPrometheusReporterTest.class.getSimpleName();

		MetricRegistry metricRegistry = new MetricRegistry();

		PushingPrometheusReporter metricReporter = PushingPrometheusReporter.forRegistry(metricRegistry)
				.host(host)
				.port(port)
				.job(job)
				.build();
		metricReporter.start(5, TimeUnit.MINUTES);
		Runtime.getRuntime().addShutdownHook(new Thread(metricReporter::stop));

		MockServer mockServer = new MockServerBuilder().withHTTPPort(port).build();
		MockServerClient mockServerClient = new MockServerClient(host, port);
		HttpRequest request = HttpRequest.request().withPath("/metrics/job/" + job);
		mockServerClient.when(request)
				.respond(HttpResponse.response().withStatusCode(HttpURLConnection.HTTP_ACCEPTED));

		HttpRequest[] requestsBefore = mockServerClient.retrieveRecordedRequests(request);
		assertThat(requestsBefore.length, is(0));

		metricRegistry.counter(PushingPrometheusReporterTest.class.getName()).inc(42);
		metricReporter.report();


		HttpRequest[] requestsAfter = mockServerClient.retrieveRecordedRequests(request);
		mockServer.stop();

		assertThat(requestsAfter.length, is(1));
		String expected = "# HELP com_github_honourednihilist_metrics_PushingPrometheusReporterTest " +
				"Generated from Dropwizard metric import (metric=com.github.honourednihilist.metrics.PushingPrometheusReporterTest, type=com.codahale.metrics.Counter)\n" +
				"# TYPE com_github_honourednihilist_metrics_PushingPrometheusReporterTest gauge\n" +
				"com_github_honourednihilist_metrics_PushingPrometheusReporterTest 42.0";
		assertThat(requestsAfter[0].getBodyAsString(), containsString(expected));
	}
}
