import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.commons.lang3.RandomUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

class JettyRequestHandler extends AbstractHandler {

	private Timer responseTimes;

	public JettyRequestHandler(MetricRegistry metrics) {
		this.responseTimes = metrics.timer("jetty.response-time");
	}

	@Override
	public void handle(String s, Request request, HttpServletRequest httpServletRequest,
					   HttpServletResponse httpServletResponse) throws IOException, ServletException {

		long start = System.nanoTime();

		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
		httpServletResponse.setContentType("text/html");

		PrintWriter writer = httpServletResponse.getWriter();
		writer.print("Your response!");

		try {
			Thread.sleep(RandomUtils.nextInt(1, 300));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		httpServletResponse.flushBuffer();
		responseTimes.update(System.nanoTime() - start, TimeUnit.NANOSECONDS);
	}
}
