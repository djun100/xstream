/*
 * Copyright (C) 2005 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008, 2009, 2011 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 22. January 2005 by Joe Walnes
 */
package com.thoughtworks.xstream.mapper;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.core.Caching;

/**
 * Mapper that caches which names map to which classes. Prevents repetitive searching and class loading.
 *
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 */
public class CachingMapper extends MapperWrapper implements Caching {

    private transient Map realClassCache;

    public CachingMapper(Mapper wrapped) {
        super(wrapped);
        readResolve();
    }

    public Class realClass(String elementName) {
        WeakReference reference = (WeakReference) realClassCache.get(elementName);
        if (reference != null) {
            Object cached = reference.get();
            if (cached instanceof CannotResolveClassException)
                throw (CannotResolveClassException) cached;
            if (cached != null) {
                return (Class)cached;
            }
        }

        try {
            Class result = super.realClass(elementName);
            realClassCache.put(elementName, new WeakReference(result));
            return result;
        } catch (CannotResolveClassException e) {
            realClassCache.put(elementName,new WeakReference(e));
            throw e;
        }
    }

    public void flushCache() {
        realClassCache.clear();
    }

    private Object readResolve() {
        realClassCache = Collections.synchronizedMap(new HashMap(128));
        return this;
    }
}
