/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.issues;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.DynamicPortTestCase;

public class InOutOutOnlyMessageCopyMule3007TestCase extends DynamicPortTestCase
{
    public InOutOutOnlyMessageCopyMule3007TestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);

    }

    @Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]{{ConfigVariant.SERVICE,
            "org/mule/issues/inout-outonly-message-copy-mule3007-test.xml"},});
    }

    @Override
    protected int getNumPortsToFind()
    {
        return 1;
    }

    @Test
    public void testStreamMessage() throws MuleException
    {
        MuleClient client = new MuleClient(muleContext);
        String url = String.format("http://localhost:%1d/services", getPorts().get(0));
        System.out.println(url);
        MuleMessage response = client.send(url, "test", null);
        assertNull(response.getExceptionPayload());
    }
}
