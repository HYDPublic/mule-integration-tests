/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.config;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.mule.runtime.api.store.ObjectStore;
import org.mule.runtime.api.store.SimpleMemoryObjectStore;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.internal.routing.IdempotentMessageValidator;
import org.mule.test.AbstractIntegrationTestCase;

import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Tests for all object stores that can be configured on an {@link IdempotentMessageValidator}.
 */
public class IdempotentMessageValidatorNamespaceHandlerTestCase extends AbstractIntegrationTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/test/config/idempotent-message-validator-config.xml";
  }

  @Test
  public void testCustomObjectStore() throws Exception {
    testPojoObjectStore("customObjectStore");
  }

  private void testPojoObjectStore(final String flowName) throws Exception {
    final Processor filter = idempotentMessageFilterFromFlow(flowName);

    final ObjectStore<?> store = getObjectStore(filter);
    assertThat(store, instanceOf(CustomObjectStore.class));

    final CustomObjectStore customStore = (CustomObjectStore) store;
    assertEquals("the-value:" + flowName, customStore.getCustomProperty());
  }

  private Processor idempotentMessageFilterFromFlow(final String flowName) throws Exception {
    final FlowConstruct flow = registry.<FlowConstruct>lookupByName(flowName).get();
    assertTrue(flow instanceof Flow);

    final Flow simpleFlow = (Flow) flow;
    final List<Processor> processors = simpleFlow.getProcessors();
    assertEquals(1, processors.size());

    final Processor firstMP = processors.get(0);
    assertThat(firstMP.getClass().getName(), equalTo("org.mule.runtime.core.internal.routing.IdempotentMessageValidator"));

    return firstMP;
  }

  private ObjectStore getObjectStore(Processor router)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = router.getClass().getMethod("getObjectStore");
    return (ObjectStore) method.invoke(router);
  }

  public static class CustomObjectStore extends SimpleMemoryObjectStore<Serializable> {

    private String customProperty;

    public String getCustomProperty() {
      return customProperty;
    }

    public void setCustomProperty(final String value) {
      customProperty = value;
    }
  }
}
