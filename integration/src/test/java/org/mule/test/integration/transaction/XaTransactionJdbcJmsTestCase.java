/*
 * $Id: JdbcMessageReceiver.java 22229 2011-06-21 06:49:29Z dirk.olmes $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.integration.transaction;

import java.util.concurrent.TimeUnit;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.functional.EventCallback;
import org.mule.tck.functional.FunctionalTestComponent;
import org.mule.util.concurrent.Latch;
import org.mule.tck.AbstractServiceAndFlowTestCase;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertThat;


public class XaTransactionJdbcJmsTestCase extends AbstractDerbyTestCase
{

    public static final int TIMEOUT = 3000;

    public XaTransactionJdbcJmsTestCase(AbstractServiceAndFlowTestCase.ConfigVariant variant, String configResources)
    {
        super(variant,configResources);
        setStartContext(false);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(
                new Object[][]{{AbstractServiceAndFlowTestCase.ConfigVariant.FLOW, "org/mule/test/integration/transaction/xa-transaction-jms-jdbc.xml"}});
    }

    @Before
    public void setUp() throws Exception
    {
        super.doSetUp();
    }

    @Override
    protected void emptyTable() throws Exception
    {
        try
        {
            execSqlUpdate("DELETE FROM TEST");
        }
        catch (Exception e)
        {
            execSqlUpdate("CREATE TABLE TEST(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0) NOT NULL PRIMARY KEY,TYPE INTEGER,DATA VARCHAR(255),ACK TIMESTAMP,RESULT VARCHAR(255))");
        }
    }

    @Test
    public void testTransactionFromJdbcToJms() throws Exception
    {
        final Latch latch = new Latch();
        MuleClient muleClient = new MuleClient(muleContext);
        muleContext.start();
        FunctionalTestComponent ftc = getFunctionalTestComponent("JdbcToJms");
        ftc.setEventCallback(new EventCallback()
        {
            public void eventReceived(MuleEventContext context, Object component) throws Exception
            {
                latch.release();
            }
        });
        execSqlUpdate("INSERT INTO TEST(TYPE, DATA) VALUES (1, 'Test1')");
        latch.await(TIMEOUT, TimeUnit.MILLISECONDS);
        MuleMessage muleMessage = muleClient.request("myQueueNotXa", TIMEOUT);
        assertThat("a message should be commit to jms",muleMessage, IsNull.<Object>notNullValue());
        assertThat("no exception should happen",muleMessage.getExceptionPayload(), IsNull.<Object>nullValue());
    }
}
