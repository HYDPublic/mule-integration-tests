/*
 * $Header$
 * $Revision$
 * $Date$
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) Cubis Limited. All rights reserved.
 * http://www.cubis.co.uk
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.test.integration.client;

import org.mule.MuleManager;
import org.mule.config.builders.MuleXmlConfigurationBuilder;
import org.mule.extras.client.MuleClient;
import org.mule.extras.client.RemoteDispatcher;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.UMOMessage;

/**
 * @author <a href="mailto:ross.mason@cubis.co.uk">Ross Mason</a>
 * @version $Revision$
 */
public class MuleClientRemotingJmsTestCase extends AbstractMuleTestCase
{
    public void setUp() throws Exception
    {
        System.setProperty("org.mule.disable.server.connections", "false");
        if(MuleManager.isInstanciated()) MuleManager.getInstance().dispose();
        MuleXmlConfigurationBuilder builder = new MuleXmlConfigurationBuilder();
        builder.configure("org/mule/test/integration/client/test-client-mule-config-remote-jms.xml");
        System.setProperty("org.mule.disable.server.connections", "true");
    }

    protected void tearDown() throws Exception
    {
        MuleManager.getInstance().dispose();
    }

    public void testClientSendToRemoteComponent() throws Exception
    {
        //Will connect to the server using tcp://localhost:60504
        MuleClient client = new MuleClient();
        MuleManager.getConfiguration().setSynchronous(true);

        RemoteDispatcher dispatcher = client.getRemoteDispatcher(getServerUrl());
        UMOMessage message = dispatcher.sendToRemoteComponent("TestReceiverUMO", "Test Client Send message", null);
        assertNotNull(message);
        assertEquals("Received: Test Client Send message", message.getPayload());
    }

    public void testClientSendAndReceiveRemote() throws Exception
    {
        String remoteEndpoint = "vm://vmRemoteProvider/remote.queue";
        //Will connect to the server using tcp://localhost:60504
        MuleClient client = new MuleClient();
        MuleManager.getConfiguration().setSynchronous(true);

        RemoteDispatcher dispatcher = client.getRemoteDispatcher(getServerUrl());
        UMOMessage message = dispatcher.receiveRemote(remoteEndpoint, 1000);
        assertNull(message);
        //We do a send instead of a dispatch here so the operation is synchronous
        //thus eaiser to test
        dispatcher.sendRemote(remoteEndpoint, "Test Remote Message 2", null);

        message = dispatcher.receiveRemote(remoteEndpoint, 100000);
        assertNotNull(message);
        assertEquals("Test Remote Message 2", message.getPayload());

    }

    public String getServerUrl()
    {
        return "jms://jmsSysProvider/mule.sys.queue?createConnector=NEVER";
    }
}
