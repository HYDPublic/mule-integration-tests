/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.integration.message;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.client.LocalMuleClient;

public class JmsPropertyScopeTestCase extends AbstractPropertyScopeTestCase
{
    public JmsPropertyScopeTestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);

    }

    @Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]{{ConfigVariant.SERVICE,
            "org/mule/test/message/jms-property-scope.xml"}

        });
    }

    @Test
    public void testRequestResponse() throws Exception
    {
        LocalMuleClient client = muleContext.getClient();
        MuleMessage message = new DefaultMuleMessage("test", muleContext);
        message.setOutboundProperty("foo", "fooValue");
        message.setReplyTo("jms://reply");

        client.dispatch("inbound", message);
        MuleMessage result = client.request("jms://reply", 10000);

        assertNotNull(result);
        assertEquals("test bar", result.getPayload());
        assertEquals("fooValue", result.<Object> getInboundProperty("foo"));
    }
}
