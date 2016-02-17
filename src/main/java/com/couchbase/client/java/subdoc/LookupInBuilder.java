/*
 * Copyright (C) 2016 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */

package com.couchbase.client.java.subdoc;

import java.util.concurrent.TimeUnit;

import com.couchbase.client.core.annotations.InterfaceAudience;
import com.couchbase.client.core.annotations.InterfaceStability;
import com.couchbase.client.core.message.ResponseStatus;
import com.couchbase.client.core.message.kv.subdoc.multi.Lookup;
import com.couchbase.client.deps.io.netty.util.internal.StringUtil;
import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.error.TranscodingException;
import com.couchbase.client.java.error.subdoc.DocumentNotJsonException;
import com.couchbase.client.java.error.subdoc.SubDocumentException;
import com.couchbase.client.java.util.Blocking;

/**
 * A builder for subdocument lookups. In order to perform the final set of operations, use the
 * {@link #doLookup()} method. Operations are performed synchronously (see {@link AsyncLookupInBuilder} for an asynchronous
 * version).
 *
 * Instances of this builder should be obtained through {@link Bucket#lookupIn(String)} rather than directly
 * constructed.
 *
 * @author Simon Baslé
 * @since 2.2
 */
@InterfaceStability.Experimental
@InterfaceAudience.Public
public class LookupInBuilder {

    private final AsyncLookupInBuilder async;
    private final long defaultTimeout;
    private final TimeUnit defaultTimeUnit;

    /**
     * Instances of this builder should be obtained through {@link Bucket#lookupIn(String)} rather than directly
     * constructed.
    */
    @InterfaceAudience.Private
    public LookupInBuilder(AsyncLookupInBuilder async, long defaultTimeout, TimeUnit defaultTimeUnit) {
        this.async = async;
        this.defaultTimeout = defaultTimeout;
        this.defaultTimeUnit = defaultTimeUnit;
    }

    /**
     * Perform several {@link Lookup lookup} operations inside a single existing {@link JsonDocument JSON document},
     * using the default key/value timeout.
     * The list of path to look for inside the JSON is constructed through builder methods {@link #get(String)} and
     * {@link #exists(String)}.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document.
     *
     * If multiple operations are specified, each spec will receive an answer, overall contained in a
     * {@link DocumentFragment}, meaning that if sub-document level error conditions happen (like the path is malformed
     * or doesn't exist), the whole operation still succeeds.
     *
     * If a single operation is specified, then any error other that a path not found will throw the corresponding
     * {@link SubDocumentException}. Otherwise a {@link DocumentFragment} is returned.
     *
     * Calling {@link DocumentFragment#content(String)} or one of its variants on a failed spec/path will throw the
     * corresponding {@link SubDocumentException}. For successful gets, it will return the value (or null in the case
     * of a path not found, and only in this case). For exists, it will return true (or false for a path not found).
     *
     * To check for any error without throwing an exception, use {@link DocumentFragment#status(String)}
     * (or its index-based variant).
     *
     * To check that a given path (or index) is valid for calling {@link DocumentFragment#content(String) content()} on
     * it without raising an Exception, use {@link DocumentFragment#exists(String)}.
     *
     * One special fatal error can also happen, when the value couldn't be decoded from JSON. In that case,
     * the ResponseStatus for the path is {@link ResponseStatus#FAILURE} and the content(path) will throw a
     * {@link TranscodingException}.
     *
     * This operation throws under the following notable error conditions:
     *
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - No lookup was defined through the builder API: {@link IllegalArgumentException}
     *
     * Other document-level error conditions are similar to those encountered during a document-level {@link AsyncBucket#get(String)}.
     *
     * @return a single {@link DocumentFragment} representing the whole list of results (1 for each spec), unless a
     * document-level error happened (in which case an exception is thrown).
     */
    public DocumentFragment<Lookup> doLookup() {
        return doLookup(defaultTimeout, defaultTimeUnit);
    }

    /**
     * Perform several {@link Lookup lookup} operations inside a single existing {@link JsonDocument JSON document},
     * using a specific timeout.
     * The list of path to look for inside the JSON is constructed through builder methods {@link #get(String)} and
     * {@link #exists(String)}.
     *
     * The subdocument API has the benefit of only transmitting the fragment of the document you work with
     * on the wire, instead of the whole document.
     *
     * If multiple operations are specified, each spec will receive an answer, overall contained in a
     * {@link DocumentFragment}, meaning that if sub-document level error conditions happen (like the path is malformed
     * or doesn't exist), the whole operation still succeeds.
     *
     * If a single operation is specified, then any error other that a path not found will throw the corresponding
     * {@link SubDocumentException}. Otherwise a {@link DocumentFragment} is returned.
     *
     * Calling {@link DocumentFragment#content(String)} or one of its variants on a failed spec/path will throw the
     * corresponding {@link SubDocumentException}. For successful gets, it will return the value (or null in the case
     * of a path not found, and only in this case). For exists, it will return true (or false for a path not found).
     *
     * To check for any error without throwing an exception, use {@link DocumentFragment#status(String)}
     * (or its index-based variant).
     *
     * To check that a given path (or index) is valid for calling {@link DocumentFragment#content(String) content()} on
     * it without raising an Exception, use {@link DocumentFragment#exists(String)}.
     *
     * One special fatal error can also happen, when the value couldn't be decoded from JSON. In that case,
     * the ResponseStatus for the path is {@link ResponseStatus#FAILURE} and the content(path) will throw a
     * {@link TranscodingException}.
     *
     * This operation throws under the following notable error conditions:
     *
     *  - The enclosing document does not exist: {@link DocumentDoesNotExistException}
     *  - The enclosing document is not JSON: {@link DocumentNotJsonException}
     *  - No lookup was defined through the builder API: {@link IllegalArgumentException}
     *
     * Other document-level error conditions are similar to those encountered during a document-level {@link AsyncBucket#get(String)}.
     *
     * @param timeout the specific timeout to apply for the operation.
     * @param timeUnit the time unit for the timeout.
     * @return a single {@link DocumentFragment} representing the whole list of results (1 for each spec), unless a
     * document-level error happened (in which case an exception is thrown).
     */
    public DocumentFragment<Lookup> doLookup(long timeout, TimeUnit timeUnit) {
        return Blocking.blockForSingle(this.async.doLookup(), timeout, timeUnit);
    }

    /**
     * Get a value inside the JSON document.
     *
     * @param path the path inside the document where to get the value from.
     * @return this builder for chaining.
     */
    public LookupInBuilder get(String path) {
        this.async.get(path);
        return this;
    }

    /**
     * Check if a value exists inside the document (if it does not, attempting to get the
     * {@link DocumentFragment#content(int)} will raise an error).
     * This doesn't transmit the value on the wire if it exists, saving the corresponding byte overhead.
     *
     * @param path the path inside the document to check for existence.
     * @return this builder for chaining.
     */
    public LookupInBuilder exists(String path) {
        this.async.exists(path);
        return this;
    }

    @Override
    public String toString() {
        return async.toString();
    }
}