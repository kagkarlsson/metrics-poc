import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;

import java.util.concurrent.TimeUnit;

public class Main {
	private static final MetricRegistry metrics = new MetricRegistry();

	public static void main(String[] args) throws Exception {

		ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.build();
		reporter.start(5, TimeUnit.SECONDS);

		new JettyServer(metrics).start();
		new RequestSimulator(metrics).start();

		Thread.sleep(1500);
	}


}
