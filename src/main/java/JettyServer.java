import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jetty9.InstrumentedConnectionFactory;
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class JettyServer {
	public static final int JETTY_THREADS = 30;
	public static final int PORT = 8181;
	private final MetricRegistry metrics;
	private Server server;

	public JettyServer(MetricRegistry metrics) {
		this.metrics = metrics;
	}

	public void start() {
		server = new Server(new QueuedThreadPool(JETTY_THREADS));
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(PORT);
		server.setConnectors(new Connector[]{connector});

		server.setHandler(new JettyRequestHandler(metrics));

		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void startWithInstrumented() {
		server = new Server(new InstrumentedQueuedThreadPool(metrics, JETTY_THREADS));
		ServerConnector connector =
				new ServerConnector(server, new InstrumentedConnectionFactory(
						new HttpConnectionFactory(), metrics.timer("jetty.connectionfactory.timer")));
		connector.setPort(PORT);
		server.setConnectors(new Connector[]{connector});

		server.setHandler(new JettyRequestHandler(metrics));

		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
