/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.transformers;

import org.mule.runtime.core.api.transformer.TransformerException;
import org.mule.runtime.core.transformer.AbstractTransformer;

import java.nio.charset.Charset;

public class FailingRuntimeTransformer extends AbstractTransformer
{

    @Override
    protected Object doTransform(Object src, Charset encoding) throws TransformerException
    {
        throw new RuntimeException("test");
    }

}
