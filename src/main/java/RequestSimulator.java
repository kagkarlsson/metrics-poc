import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RequestSimulator {

	private final CloseableHttpClient httpclient;
	private final ThreadPoolExecutor httpCaller;
	private final Timer requesttimer;

	public RequestSimulator(MetricRegistry metrics) {
		final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		httpclient = HttpClientBuilder.create().setConnectionManager(connectionManager).build();

		final LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(10000);
		httpCaller = new ThreadPoolExecutor(2, 25, 5, TimeUnit.MINUTES, workQueue);

		metrics.register("httpclient.connectionpool.utilization", new Gauge<Double>() {
			public Double getValue() {
				PoolStats totalStats = connectionManager.getTotalStats();
				return ((double) totalStats.getLeased()) / totalStats.getMax();
			}
		});
		requesttimer = metrics.timer("requests.requesttime");

		metrics.register("executor.queuesize",
				new Gauge<Integer>() {
					public Integer getValue() {
						return workQueue.size();
					}
				});
	}

	public void start() throws Exception {

		for (int i = 0; i < 1000; i++) {
			httpCaller.submit(new Runnable() {
				@Override
				public void run() {

					try (Timer.Context timerContext = requesttimer.time()) {

						CloseableHttpResponse response = httpclient.execute(new HttpHost("localhost", JettyServer.PORT), new HttpGet());
						response.close();

					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
	}
}
