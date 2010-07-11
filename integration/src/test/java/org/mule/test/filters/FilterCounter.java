/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.filters;

import org.mule.api.MuleMessage;
import org.mule.api.routing.filter.Filter;
import org.mule.api.transport.PropertyScope;

import java.util.concurrent.atomic.AtomicInteger;

public class FilterCounter implements Filter
{
    public static AtomicInteger counter = new AtomicInteger();
    
    /**
     * Increments the counter if it passes the filter 
     */
    public boolean accept(MuleMessage message)
    {
        if(message.getProperty("pass", PropertyScope.INBOUND) != null && message.getProperty("pass", PropertyScope.INBOUND).equals("true"))
        {
            counter.incrementAndGet();
            return true;
        }
        return false;
    }

    public boolean test(int arg0)
    {
        // TODO Auto-generated method stub
        return false;
    }

}

