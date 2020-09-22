package org.infinispan.wfink.playground.counter;

import java.util.concurrent.CompletableFuture;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCounterManagerFactory;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.counter.api.CounterManager;
import org.infinispan.counter.api.StrongCounter;

/**
 * A simple client which use Clustered Counters to show the behaviour of strong counter use. The client should be started several times.
 *
 * @author <a href="mailto:WolfDieter.Fink@gmail.com">Wolf-Dieter Fink</a>
 */
public class StrongCounterHotRodClient {
  private RemoteCacheManager remoteCacheManager;
  private StrongCounter strongCounter;

  public StrongCounterHotRodClient(String host, String port, String counterName) {
    ConfigurationBuilder remoteBuilder = new ConfigurationBuilder();
    remoteBuilder.addServer().host(host).port(Integer.parseInt(port));
    // use a second node for a cluster
    remoteBuilder.addServer().host(host).port(Integer.parseInt(port) + 100);

    remoteCacheManager = new RemoteCacheManager(remoteBuilder.build());
    CounterManager counterManager = RemoteCounterManagerFactory.asCounterManager(remoteCacheManager);
    strongCounter = counterManager.getStrongCounter(counterName);

    // uncomment to see how the Listener works
    strongCounter.addListener(new StrongCounterListener());

    if (strongCounter == null) {
      throw new RuntimeException("StrongCounter '" + counterName + "' not found. Please make sure the server is properly configured");
    }
  }

  private void stop() {
    remoteCacheManager.stop();
  }

  private void loop() {
    for (int i = 0; i < 100; i++) {
      try {
        System.out.println("LOOP " + i);
        CompletableFuture<Long> value = strongCounter.incrementAndGet();
        System.out.println("Counter Value : " + value.join());
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // ignore
      } catch (Exception e) {
        try {
          // prevent from mixed output with EventListener
          Thread.sleep(500);
        } catch (InterruptedException e2) {
        }
        e.printStackTrace();
        try {
          Thread.sleep(4500);
        } catch (InterruptedException e1) {
        }
      }
    }
    System.out.println("LOOP done");
  }

  public static void main(String[] args) {
    String host = "localhost";
    String port = "11222";
    String cacheName = "StrongCounter";

    if (args.length > 0) {
      port = args[0];
    }
    if (args.length > 1) {
      port = args[1];
    }
    StrongCounterHotRodClient client = new StrongCounterHotRodClient(host, port, cacheName);

    client.loop();

    client.stop();
    System.out.println("\nDone !");
  }
}
