/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.routing;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mule.functional.api.component.FunctionalTestProcessor.getFromFlow;
import static org.mule.runtime.api.notification.RoutingNotification.CORRELATION_TIMEOUT;

import org.mule.functional.api.component.FunctionalTestProcessor;
import org.mule.functional.api.component.TestConnectorQueueHandler;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.notification.IntegerAction;
import org.mule.runtime.api.notification.RoutingNotification;
import org.mule.runtime.api.notification.RoutingNotificationListener;
import org.mule.tck.probe.PollingProber;
import org.mule.tck.probe.Probe;
import org.mule.test.AbstractIntegrationTestCase;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CollectionAggregatorRouterTimeoutTestCase extends AbstractIntegrationTestCase {

  @Override
  protected String getConfigFile() {
    return "collection-aggregator-router-timeout-test-flow.xml";
  }

  @Test
  public void testNoFailOnTimeout() throws Exception {
    // correlation timeouts should not fire in this scenario, check it
    final AtomicInteger correlationTimeoutCount = new AtomicInteger(0);
    notificationListenerRegistry.registerListener(new RoutingNotificationListener<RoutingNotification>() {

      @Override
      public boolean isBlocking() {
        return false;
      }

      @Override
      public void onNotification(RoutingNotification notification) {
        if (new IntegerAction(CORRELATION_TIMEOUT).equals(notification.getAction())) {
          correlationTimeoutCount.incrementAndGet();
        }
      }
    });

    FunctionalTestProcessor vortex = getFromFlow(locator, "vortex");
    FunctionalTestProcessor aggregator = getFromFlow(locator, "aggregator");

    List<String> list = asList("first", "second");

    flowRunner("splitter").withPayload(list).run();

    Thread.sleep(RECEIVE_TIMEOUT);

    // no correlation timeout should ever fire
    assertThat("GroupCorrelation timeout should not have happened.", correlationTimeoutCount.intValue(), is(0));

    // should receive only the second message
    assertThat("Vortex received wrong number of messages.", vortex.getReceivedMessagesCount(), is(1));
    assertThat("Wrong message received", vortex.getLastReceivedMessage().getMessage().getPayload().getValue(), is("second"));

    // should receive only the first part
    assertThat("Aggregator received wrong number of messages.", aggregator.getReceivedMessagesCount(), is(1));
    assertThat("Wrong message received",
               ((List<Message>) aggregator.getLastReceivedMessage().getMessage().getPayload().getValue()).get(0).getPayload()
                   .getValue(),
               is("first"));

    // wait for the vortex timeout (6000ms for vortext + 2000ms for aggregator
    // timeout + some extra for a test)
    new PollingProber(2 * RECEIVE_TIMEOUT, 200).check(new Probe() {

      @Override
      public boolean isSatisfied() {
        // now get the messages which were lagging behind
        // it will receive only one (first) as second will be discarded by the worker
        // because it has already dispatched one with the same group id
        return aggregator.getReceivedMessagesCount() == 1;
      }

      @Override
      public String describeFailure() {
        return "Other messages never received by aggregator.";
      }
    });

    TestConnectorQueueHandler queueHandler = new TestConnectorQueueHandler(registry);
    assertThat(queueHandler.read("out", RECEIVE_TIMEOUT), is(notNullValue()));
  }
}
