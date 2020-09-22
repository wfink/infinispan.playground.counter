package org.infinispan.wfink.playground.counter;

import org.infinispan.counter.api.CounterEvent;

public class StrongCounterListener implements org.infinispan.counter.api.CounterListener {

  @Override
  public void onUpdate(CounterEvent entry) {
    System.out.println("EVENT " + entry);
  }

}
