package org.infinispan.wfink.playground.counter;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCounterManagerFactory;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.counter.api.CounterManager;
import org.infinispan.counter.api.WeakCounter;

/**
 * A simple client which use Clustered Counters to show the behaviour of strong counter use. The client should be started several times.
 *
 * @author <a href="mailto:WolfDieter.Fink@gmail.com">Wolf-Dieter Fink</a>
 */
public class WeakCounterHotRodClient {
  private RemoteCacheManager remoteCacheManager;
  private WeakCounter weakCounter;

  public WeakCounterHotRodClient(String host, String port, String counterName) {
    ConfigurationBuilder remoteBuilder = new ConfigurationBuilder();
    remoteBuilder.addServer().host(host).port(Integer.parseInt(port));
    // use a second node for a cluster
    remoteBuilder.addServer().host(host).port(Integer.parseInt(port) + 100);

    remoteCacheManager = new RemoteCacheManager(remoteBuilder.build());
    CounterManager counterManager = RemoteCounterManagerFactory.asCounterManager(remoteCacheManager);
    weakCounter = counterManager.getWeakCounter(counterName);

    if (weakCounter == null) {
      throw new RuntimeException("WeakCounter '" + counterName + "' not found. Please make sure the server is properly configured");
    }
  }

  private void stop() {
    remoteCacheManager.stop();
  }

  private void loop() {
    System.out.println("WeakCounter value: " + weakCounter.getValue());
    for (int i = 0; i < 10000; i++) {
      try {
        System.out.println("LOOP " + i);
        Long last = weakCounter.getValue();
        System.out.println("INC ");
        weakCounter.add(1);
        weakCounter.add(1).get();
        Long now = weakCounter.getValue();
        if (last + 2 != now) {
          throw new Exception("MISSED");
        }
        System.out.println("Counter Value : " + weakCounter.getValue());
      } catch (InterruptedException e) {
        // ignore
      } catch (Exception e) {
        e.printStackTrace();
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e1) {
        }
      }
    }
    System.out.println("LOOP done");
  }

  public static void main(String[] args) {
    String host = "localhost";
    String port = "11222";
    String cacheName = "WeakCounter";

    if (args.length > 0) {
      port = args[0];
    }
    if (args.length > 1) {
      port = args[1];
    }
    WeakCounterHotRodClient client = new WeakCounterHotRodClient(host, port, cacheName);

    client.loop();

    client.stop();
  }
}
