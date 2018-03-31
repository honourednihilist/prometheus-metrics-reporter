package com.github.honourednihilist.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;

import java.io.Closeable;
import java.io.IOException;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.HTTPServer;

/**
 * A reporter class for exporting metrics values to Prometheus via HTTP
 */
public class PullablePrometheusReporter implements Reporter, Closeable {

	/**
	 * Returns a new {@link Builder} for {@link PullablePrometheusReporter}.
	 *
	 * @param registry the registry to report
	 * @return a {@link Builder} instance for a {@link PullablePrometheusReporter}
	 */
	public static Builder forRegistry(MetricRegistry registry) {
		return new Builder(registry);
	}

	/**
	 * A builder for {@link PullablePrometheusReporter} instances.
	 */
	public static class Builder {
		private final MetricRegistry registry;
		private int port;

		private Builder(MetricRegistry registry) {
			this.registry = registry;
		}

		/**
		 * The port number to bind http-server to
		 */
		public Builder port(int port) {
			this.port = port;
			return this;
		}

		/**
		 * Builds a {@link PullablePrometheusReporter} with the given properties.
		 *
		 * @return a {@link PullablePrometheusReporter}
		 */
		public PullablePrometheusReporter build() {
			return new PullablePrometheusReporter(registry, port);
		}
	}

	private final MetricRegistry registry;
	private final int port;

	private DropwizardExports prometheusCollector;
	private HTTPServer httpServer;

	private PullablePrometheusReporter(MetricRegistry registry, int port) {
		this.registry = registry;
		this.port = port;
	}

	/**
	 * Starts the reporter.
	 */
	public synchronized void start() throws IOException {
		prometheusCollector = new DropwizardExports(registry);
		CollectorRegistry.defaultRegistry.register(prometheusCollector);
		httpServer = new HTTPServer(port, true);
	}

	/**
	 * Stops the reporter.
	 */
	public synchronized void stop() {
		httpServer.stop();
		httpServer = null;

		CollectorRegistry.defaultRegistry.unregister(prometheusCollector);
		prometheusCollector = null;
	}

	/**
	 * Stops the reporter.
	 */
	@Override
	public void close() {
		stop();
	}
}
