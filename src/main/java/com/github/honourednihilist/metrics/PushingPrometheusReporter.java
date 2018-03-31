package com.github.honourednihilist.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.PushGateway;

/**
 * A reporter class for exporting metrics values to Prometheus via Pushgateway
 */
public class PushingPrometheusReporter extends ScheduledReporter {

	/**
	 * Returns a new {@link Builder} for {@link PushingPrometheusReporter}.
	 *
	 * @param registry the registry to report
	 * @return a {@link Builder} instance for a {@link PushingPrometheusReporter}
	 */
	public static Builder forRegistry(MetricRegistry registry) {
		return new Builder(registry);
	}

	/**
	 * A builder for {@link PushingPrometheusReporter} instances.
	 */
	public static class Builder {
		private final MetricRegistry registry;
		private String host;
		private int port;
		private String job;
		private TimeUnit rateUnit;
		private TimeUnit durationUnit;

		private Builder(MetricRegistry registry) {
			this.registry = registry;
			this.rateUnit = TimeUnit.SECONDS;
			this.durationUnit = TimeUnit.MILLISECONDS;
		}

		/**
		 * The host of Push gateway
		 */
		public Builder host(String host) {
			this.host = host;
			return this;
		}

		/**
		 * The port number of Push gateway
		 */
		public Builder port(int port) {
			this.port = port;
			return this;
		}

		/**
		 * The job name of metrics
		 */
		public Builder job(String job) {
			this.job = job;
			return this;
		}

		/**
		 * Convert all the rates to a certain timeunit, defaults to seconds
		 */
		public Builder convertRatesTo(TimeUnit rateUnit) {
			this.rateUnit = rateUnit;
			return this;
		}

		/**
		 * Convert all the durations to a certain timeunit, defaults to milliseconds
		 */
		public Builder convertDurationsTo(TimeUnit durationUnit) {
			this.durationUnit = durationUnit;
			return this;
		}

		/**
		 * Builds a {@link PullablePrometheusReporter} with the given properties.
		 *
		 * @return a {@link PullablePrometheusReporter}
		 */
		public PushingPrometheusReporter build() {
			return new PushingPrometheusReporter(registry, host, port, job, rateUnit, durationUnit);
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(PushingPrometheusReporter.class);

	private final CollectorRegistry prometheusRegistry;
	private final String address;
	private final PushGateway pushGateway;
	private final String job;

	private PushingPrometheusReporter(MetricRegistry registry, String host, int port, String job, TimeUnit rateUnit, TimeUnit durationUnit) {
		super(registry, "pushgateway-prometheus-reporter", MetricFilter.ALL, rateUnit, durationUnit);
		prometheusRegistry = new CollectorRegistry();
		prometheusRegistry.register(new DropwizardExports(registry));
		address = host + ":" + port;
		pushGateway = new PushGateway(address);
		this.job = job;
	}

	@Override
	public void report(SortedMap<String, Gauge> gauges,
	                   SortedMap<String, Counter> counters,
	                   SortedMap<String, Histogram> histograms,
	                   SortedMap<String, Meter> meters,
	                   SortedMap<String, Timer> timers) {
		try {
			pushGateway.push(prometheusRegistry, job);
		} catch (IOException e) {
			LOGGER.error("Can't report Metrics to Prometheus Pushgateway [" + address + "]", e);
		}
	}
}
