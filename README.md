# Metrics Prometheus Reporters

These are reporters for the excellent [Metrics library](http://metrics.dropwizard.io/), 
similar to the [Elasticsearch](https://github.com/elastic/elasticsearch-metrics-reporter-java) reporter, 
except that it reports to a Prometheus Pushgateway or it is pulled by a Prometheus agent.

## Installation

Add a repository and a dependency: 
```groovy
repositories { 
    maven { 
        url "https://dl.bintray.com/honourednihilist/maven" 
    } 
}

dependencies {
    compile(group: 'com.github.honourednihilist', name:'prometheus-metrics-reporter', version: '0.1.0')
}
```

## Usage

**PullablePrometheusReporter** exports metrics values to Prometheus via HTTP. 
It is Prometheus's usual pull model for general metrics collection:

```java
MetricRegistry metricRegistry = new MetricRegistry();
PullablePrometheusReporter metricReporter = PullablePrometheusReporter.forRegistry(metricRegistry)
    .port(9501)
    .build();
metricReporter.start();
Runtime.getRuntime().addShutdownHook(new Thread(metricReporter::stop));

metricRegistry.counter("my-counter").inc(42);
```

Now you may request collected metrics:
```bash
$ curl http://localhost:9501/metrics
```

---

**PushingPrometheusReporter** reports metrics values to Prometheus via Pushgateway.
Usually, Pushgateway is used for capturing the outcome of a service-level batch job.

```java
MetricRegistry metricRegistry = new MetricRegistry();

PushingPrometheusReporter metricReporter = PushingPrometheusReporter.forRegistry(metricRegistry)
    .host("pushgateway-host")
    .port(9091)
    .job("my-job")
    .build();
metricReporter.start(5, TimeUnit.MINUTES);
Runtime.getRuntime().addShutdownHook(new Thread(metricReporter::stop));

metricRegistry.counter("my-counter").inc(42);
metricReporter.report(); 
```
