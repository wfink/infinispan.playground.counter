Infinispan Clustered Counters
===============================

Author: Wolf-Dieter Fink
Level: Basics
Technologies: Infinispan, Hot Rod, Counter


What is it?
-----------

Examples how to use the different types of clustered counters with remote infinispan servers

Hot Rod is a binary TCP client-server protocol. The Hot Rod protocol facilitates faster client and server interactions in comparison to other text based protocols and allows clients to make decisions about load balancing, failover and data location operations.

This example demonstrates how to use Strong and Weak clustered counters


Prepare a server instance
-------------
Simple start a Infinispan 10+ or RHDG 8+ server and add the following configuration to cache-container within the infinispan.xml

      <counters xmlns="urn:infinispan:config:counters:11.0" num-owners="2" reliability="CONSISTENT">
        <strong-counter name="StrongCounter" initial-value="0" storage="VOLATILE"/>
        <weak-counter name="WeakCounter" initial-value="0" storage="VOLATILE"/>
      </counters>

It is possible to start 1 or more servers, the code will use the server on localhost with the default port 11222 and +100 offset.

     bin/server.sh [-o 100] -n <node name> [-s <server directory>]


Build and Run the examples
-------------------------
1. Type this command to build :

        mvn clean package

2. Use a simple strong counter

   Use maven to start one or more clients for StrongCounter

         mvn exec:java -Dexec.mainClass="org.infinispan.wfink.playground.counter.StrongCounterHotRodClient"

   This example will increment the counter 100 times 


3. Use bounded strong counter

   Change the StrongCounterHotRodClient and uncomment the addListener(...) method
   Add the attribute upper-bound to the strong-counter element

   Use maven to run the client again

         mvn exec:java -Dexec.mainClass="org.infinispan.wfink.playground.counter.StrongCounterHotRodClient"

   If the upper-bound is reached the Listener will show the state 'UPPER_BOUND_REACHED' and the operation will throw a CounterOutOfBoundsException.


4. Use simple weak counter

   Use maven to run the weak client

         mvn exec:java -Dexec.mainClass="org.infinispan.wfink.playground.counter.WeakCounterHotRodClient"

   The client will use the weak counter and increment the counter two times in a loop for 10,000 retries
   With a single node there is no failure and the client will run successfully
   If starting two or more nodes the possibility of concurrent updates is most likely and the client might stop for 5sec with the 'java.lang.Exception: MISSED' 
   to show that one or both increments are lost, this is an effect of the weak counter as it works asynchronous.
