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
import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.module.client.MuleClient;
import org.mule.tck.DynamicPortTestCase;
import org.mule.transformer.simple.ObjectToString;

public class HttpReturnsJaxbObject5531TestCase extends DynamicPortTestCase
{
    public HttpReturnsJaxbObject5531TestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);

    }

    private static final String ZIP_RESPONSE = "<?xml version='1.0' encoding='utf-8'?><soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/' "
                                               + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema'>"
                                               + "<soap:Body><GetCityWeatherByZIPResponse xmlns='http://ws.cdyne.com/WeatherWS/'><GetCityWeatherByZIPResult>"
                                               + "<Success>true</Success><ResponseText>City Found</ResponseText><State>GA</State><City>Roswell</City>"
                                               + "<WeatherStationCity>Marietta</WeatherStationCity><WeatherID>1</WeatherID><Description>Thunder Storms</Description>"
                                               + "<Temperature>79</Temperature><RelativeHumidity>57</RelativeHumidity><Wind>S8</Wind>"
                                               + "<Pressure>29.91R</Pressure><Visibility /><WindChill /><Remarks /></GetCityWeatherByZIPResult>"
                                               + "</GetCityWeatherByZIPResponse></soap:Body></soap:Envelope>";

    @Override
    protected int getNumPortsToFind()
    {
        return 1;
    }

    @Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]{{ConfigVariant.SERVICE,
            "org/mule/issues/http-returns-jaxb-object-mule-5531-test.xml"}

        });
    }

    @Test
    public void testGetWeather() throws Exception
    {
        String testUrl = "http://localhost:" + getPorts().get(0) + "/test/weather";
        MuleClient client = new MuleClient(muleContext);
        Object response = client.send(testUrl, "hello", null);
        assertNotNull(response);
        String stringResponse = (String) new ObjectToString().transform(response, "UTF-8");
        assertTrue(stringResponse.contains("<Success>true</Success>"));
    }

    public static class WeatherReport implements Callable
    {
        public Object onCall(MuleEventContext eventContext) throws Exception
        {
            return ZIP_RESPONSE;
        }
    }
}
