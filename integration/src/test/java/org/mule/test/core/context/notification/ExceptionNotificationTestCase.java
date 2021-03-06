/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.core.context.notification;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertNull;
import static org.mule.runtime.api.notification.ExceptionNotification.EXCEPTION_ACTION;

import org.mule.functional.api.exception.FunctionalTestException;
import org.mule.functional.listener.ExceptionListener;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.notification.ExceptionNotification;
import org.mule.runtime.api.notification.IntegerAction;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ExceptionNotificationTestCase extends AbstractNotificationTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Override
  protected String getConfigFile() {
    return "org/mule/test/integration/notifications/exception-notification-test-flow.xml";
  }

  @Test
  public void doTest() throws Exception {
    ExceptionListener exceptionListener = new ExceptionListener(notificationListenerRegistry);
    expectedException.expectCause(instanceOf(FunctionalTestException.class));
    Message result = flowRunner("the-service").withPayload(TEST_PAYLOAD).run().getMessage();
    // processing is async, give time for the exception notificator to run
    exceptionListener.waitUntilAllNotificationsAreReceived();

    assertNull(result);

    assertNotifications();
  }

  @Override
  public RestrictedNode getSpecification() {
    return new Node(ExceptionNotification.class, new IntegerAction(EXCEPTION_ACTION));
  }

  @Override
  public void validateSpecification(RestrictedNode spec) throws Exception {
    verifyAllNotifications(spec, ExceptionNotification.class, EXCEPTION_ACTION, EXCEPTION_ACTION);
  }
}
