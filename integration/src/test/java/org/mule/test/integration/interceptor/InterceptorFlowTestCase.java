/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.integration.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.mule.api.MuleMessage;
import org.mule.api.client.MuleClient;
import org.mule.functional.junit4.FunctionalTestCase;

import org.junit.Test;

public class InterceptorFlowTestCase extends FunctionalTestCase
{
    @Test
    public void testDefaultJavaComponentShortcut() throws Exception
    {
        MuleClient client = muleContext.getClient();
        client.dispatch("in", "hello world", null);
        MuleMessage message = client.request("out", 3000);
        assertNotNull(message);
        String payload = (String)message.getPayload();
        assertNotNull(payload);
        //note that there is an exclamation mark on the end that was added by the interceptor
        assertEquals("hello world!", payload);
    }

    @Override
    protected String getConfigFile()
    {
        return "org/mule/test/integration/interceptor-flow.xml";
    }
}
