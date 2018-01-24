/*******************************************************************************
 * Copyright (c) 2015 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.ibm.ws.lars.rest.memorybackend;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import com.ibm.ws.lars.rest.AssetFilter;
import com.ibm.ws.lars.rest.Condition;
import com.ibm.ws.lars.rest.Condition.Operation;
import com.ibm.ws.lars.rest.PaginationOptions;
import com.ibm.ws.lars.rest.Persistor;
import com.ibm.ws.lars.rest.RepositoryRESTResource;
import com.ibm.ws.lars.rest.SortOptions;
import com.ibm.ws.lars.rest.SortOptions.SortOrder;
import com.ibm.ws.lars.rest.exceptions.AssetPersistenceException;
import com.ibm.ws.lars.rest.exceptions.InvalidJsonAssetException;
import com.ibm.ws.lars.rest.exceptions.NonExistentArtefactException;
import com.ibm.ws.lars.rest.model.Asset;
import com.ibm.ws.lars.rest.model.AssetCursor;
import com.ibm.ws.lars.rest.model.Attachment;
import com.ibm.ws.lars.rest.model.AttachmentContentMetadata;
import com.ibm.ws.lars.rest.model.AttachmentContentResponse;
import com.ibm.ws.lars.rest.model.AttachmentList;

/**
 * Alternative Persistor implementation which just stores the data in memory.
 * <p>
 * This makes it entirely unsuitable production use or even testing with large amounts of data, but
 * it does allow small-scale testing without needing a database.
 * <p>
 * This class is annotated as an Alternative with a Priority, which means that if it's included in
 * the application, it will be used instead of the regular MongoDB Persistor.
 */
@ApplicationScoped
@Alternative
@Priority(APPLICATION)
public class MemoryPersistor implements Persistor {

    static private long lastId = 0;

    private static synchronized String getNextId() {
        lastId++;
        return String.format("%024x", lastId);
    }

    private static final String ASSET_ID = "assetId";

    private static final String MATCH_SCORE = "$matchScore";

    private final Map<String, Map<String, Object>> assets = new HashMap<>();

    private final Map<String, Map<String, Object>> attachments = new HashMap<>();

    private final Map<String, AttachmentContent> gridFS = new HashMap<>();

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.ws.lars.rest.Persistor#retrieveAllAssets()
     */
    @Override
    public AssetCursor retrieveAllAssets() {
        // Note: retrieveAllAssets does *not* set attachments
        return new BasicAssetCursor(assets.values());
    }

    @Override
    public AssetCursor retrieveAllAssets(Collection<AssetFilter> filters, String searchTerm, PaginationOptions pagination, SortOptions sortOptions) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> asset : assets.values()) {
            if (searchTerm != null && !searchFinds(asset, searchTerm)) {
                continue;
            }

            if (filters != null && !filtersMatch(asset, filters)) {
                continue;
            }

            results.add(asset);
        }

        if (searchTerm != null && sortOptions == null) {
            sortOptions = new SortOptions(MATCH_SCORE, SortOrder.DESCENDING);
        }

        sort(results, sortOptions);
        results = paginate(results, pagination);

        for (Map<?, ?> result : results) {
            result.remove(MATCH_SCORE);
        }
        return new BasicAssetCursor(results);
    }

    @Override
    public List<Object> getDistinctValues(String field, Collection<AssetFilter> filters, String searchTerm) {
        Set<Object> results = new HashSet<Object>();

        for (Map<String, Object> asset : assets.values()) {
            if (searchTerm != null && !searchFinds(asset, searchTerm)) {
                continue;
            }

            if (filters != null && !filtersMatch(asset, filters)) {
                continue;
            }

            Object value = getValue(asset, field);
            if (value != null) {
                results.add(value);
            }
        }

        return new ArrayList<Object>(results);
    }

    /** {@inheritDoc} */
    @Override
    public int countAllAssets(Collection<AssetFilter> filters, String searchTerm) {
        int count = 0;
        for (Map<String, Object> asset : assets.values()) {
            if (searchTerm != null && !searchFinds(asset, searchTerm)) {
                continue;
            }

            if (filters != null && !filtersMatch(asset, filters)) {
                continue;
            }

            count++;
        }

        return count;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.ws.lars.rest.Persistor#retrieveAsset(java.lang.String)
     */
    @Override
    public Asset retrieveAsset(String assetId) throws NonExistentArtefactException {
        if (!assets.containsKey(assetId)) {
            throw new NonExistentArtefactException(assetId.toString(), RepositoryRESTResource.ArtefactType.ASSET);
        }
        return Asset.createAssetFromMap(new HashMap<>(assets.get(assetId)));
    }

    @Override
    public Asset createAsset(Asset newAsset) throws InvalidJsonAssetException {
        Map<String, Object> props = newAsset.getProperties();
        String id = getNextId();
        props.put("_id", id);
        assets.put(id, props);
        return Asset.createAssetFromMap(props);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.ws.lars.rest.Persistor#deleteAsset(java.lang.String)
     */
    @Override
    public void deleteAsset(String assetId) {
        assets.remove(assetId);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.ws.lars.rest.Persistor#updateAsset(java.lang.String,
     * com.ibm.ws.lars.rest.model.Asset)
     */
    @Override
    public Asset updateAsset(String assetId, Asset asset) throws InvalidJsonAssetException, NonExistentArtefactException {
        assets.put(assetId, asset.getProperties());
        return asset;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.ws.lars.rest.Persistor#findAttachmentsForAsset(java.lang.String)
     */
    @Override
    public AttachmentList findAttachmentsForAsset(String assetId) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Entry<String, Map<String, Object>> e : attachments.entrySet()) {
            Map<String, Object> attachmentState = e.getValue();
            if (Objects.equals(attachmentState.get(ASSET_ID), assetId)) {
                resultList.add(attachmentState);
            }
        }

        return AttachmentList.createAttachmentListFromMaps(resultList);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.ws.lars.rest.Persistor#createAttachmentContent(java.lang.String,
     * java.lang.String, java.io.InputStream)
     */
    @Override
    public AttachmentContentMetadata createAttachmentContent(String name, String contentType, InputStream attachmentContentStream) throws AssetPersistenceException {
        try {
            String id = getNextId();

            // Oh Java, I hate you for making me do this. Maybe if we start using
            // Apache Commons then wecan rip this out
            byte[] buffer = new byte[1024];
            int length;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((length = attachmentContentStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            byte[] contentBytes = baos.toByteArray();

            AttachmentContent attachmentContent = new AttachmentContent(name, contentType, id, contentBytes);

            gridFS.put(id, attachmentContent);

            return new AttachmentContentMetadata(id, contentBytes.length);
        } catch (IOException e) {
            throw new AssetPersistenceException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ibm.ws.lars.rest.Persistor#createAttachmentMetadata(com.ibm.ws.lars.rest.model.Attachment
     * )
     */
    @Override
    public Attachment createAttachmentMetadata(Attachment attachment) {
        Map<String, Object> props = new HashMap<>(attachment.getProperties());
        String id = attachment.get_id();

        if (id == null) {
            id = getNextId();
            attachment.set_id(id);
        }
        attachments.put(id, props);
        return Attachment.createAttachmentFromMap(props);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.ws.lars.rest.Persistor#retrieveAttachmentMetadata(java.lang.String)
     */
    @Override
    public Attachment retrieveAttachmentMetadata(String attachmentId) throws NonExistentArtefactException {
        if (!attachments.containsKey(attachmentId)) {
            throw new NonExistentArtefactException(attachmentId, RepositoryRESTResource.ArtefactType.ATTACHMENT);
        }
        return Attachment.createAttachmentFromMap(new HashMap<>(attachments.get(attachmentId)));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.ws.lars.rest.Persistor#deleteAttachmentContent(java.lang.String)
     */
    @Override
    public void deleteAttachmentContent(String attachmentId) {
        gridFS.remove(attachmentId);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.ws.lars.rest.Persistor#deleteAttachmentMetadata(java.lang.String)
     */
    @Override
    public void deleteAttachmentMetadata(String attachmentId) {
        attachments.remove(attachmentId);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.ws.lars.rest.Persistor#retrieveAttachmentContent(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public AttachmentContentResponse retrieveAttachmentContent(String gridFSId) {
        AttachmentContent content = gridFS.get(gridFSId);
        InputStream contentStream = new ByteArrayInputStream(content.content);
        String contentType = content.contentType;
        return new AttachmentContentResponse(contentStream, contentType);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.ws.lars.rest.Persistor#allocateNewId()
     */
    @Override
    public String allocateNewId() {
        return getNextId();
    }

    /** {@inheritDoc} */
    @Override
    public void initialize() {
        // Nothing to be done
    }

    /**
     * Checks whether an asset object matches all of a set of filters
     *
     * @param object the asset object
     * @param filters the filters to check
     * @return true if the asset matches all of the filters
     */
    private static boolean filtersMatch(Map<?, ?> object, Collection<AssetFilter> filters) {
        for (AssetFilter filter : filters) {
            if (!matches(object, filter)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether an asset matches a filter
     * <p>
     * An asset matches a filter if the value for the key matches any of the filter conditions
     *
     * @param object the asset object
     * @param filter the filter
     * @return true if object matches filter
     */
    private static boolean matches(Map<?, ?> object, AssetFilter filter) {
        Object value = getValue(object, filter.getKey());
        for (Condition condition : filter.getConditions()) {
            if (matches(value, condition)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether an value matches a filter condition
     * <p>
     * If the value is a list, it checks whether any value in the list matches the condition
     *
     * @param value the value to check
     * @param condition the condition to check against
     * @return true if the value matches the condition
     */
    private static boolean matches(Object value, Condition condition) {
        if (value instanceof List<?>) {
            for (Object valueElement : (List<?>) value) {
                if (matches(valueElement, condition)) {
                    return true;
                }
            }
            return false;
        }

        boolean matches = condition.getValue().equals(value);
        if (matches && condition.getOperation() == Operation.EQUALS) {
            return true;
        } else if (!matches && condition.getOperation() == Operation.NOT_EQUALS) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks whether a search should find a given asset and computes the match score
     * <p>
     * Checks whether a search for the given string should return the given asset.
     * <p>
     * As a side effect, it adds a new property named {@link #MATCH_SCORE} with the match score. The
     * match score is computed by counting how many of the search terms are matched.
     *
     * @param object the asset object
     * @param searchString the search string
     * @return true if a search for the search string should return the asset
     */
    private static boolean searchFinds(Map<String, Object> object, String searchString) {
        /*
         * The logic here is a bit random but matches MongoDB's logic First split the search string
         * into tokens: * Negations (e.g. -foo) * Phrases (e.g. "my wibble") * Words (e.g. bar)
         */

        int score = 0; // match score

        SearchStringParser parser = new SearchStringParser(searchString);
        List<String> fieldsToCheck = Arrays.asList("name", "description", "shortDescription", "tags");

        // object must not match any negations
        for (String negation : parser.negations) {
            if (fieldsContain(object, fieldsToCheck, negation)) {
                return false;
            }
        }

        // Object must match all phrases
        for (String phrase : parser.phrases) {
            if (!fieldsContain(object, fieldsToCheck, phrase)) {
                return false;
            } else {
                score++;
            }
        }

        boolean result = false;

        // If there are any phrases and it matches them all, return true
        // We need to keep checking though to calculate the score
        if (!parser.phrases.isEmpty()) {
            result = true;
        }

        // Object must match any word
        for (String word : parser.words) {
            if (fieldsContain(object, fieldsToCheck, word)) {
                result = true;
                score++;
            }
        }

        object.put(MATCH_SCORE, String.format("%05d", score)); //zero-padded match score for sorting

        return result;
    }

    /**
     * Checks whether certain fields in an asset contain the given substring
     *
     * @param object the asset object
     * @param fieldsToCheck a list of fields to look at
     * @param string the substring to look for
     * @return true if any of the fieldsToCheck in the object contain string as a substring
     */
    private static boolean fieldsContain(Map<?, ?> object, List<String> fieldsToCheck, String string) {
        string = string.toLowerCase();
        for (String field : fieldsToCheck) {
            Object value = getValue(object, field);
            if (value instanceof String) {
                String stringValue = (String) value;
                if (stringValue.toLowerCase().contains(string)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Extract a value from an asset object by looking it up by path.
     * <p>
     * This broadly matches the mongodb query syntax
     * <p>
     * E.g. with an object
     *
     * <pre>
     * { "foo" : "bar" }
     *
     * </pre>
     *
     * using the key {@code foo} returns {@code bar}
     * <p>
     * E.g. with an object
     *
     * <pre>
     * { "foo" :
     *     {
     *       "bar" : "baz"
     *     }
     * }
     * </pre>
     *
     * using the key {@code foo.bar} returns {@code baz}
     * <p>
     * If one of the elements in the path is a list, the result will be a list of objects
     * <p>
     * E.g. with an object
     *
     * <pre>
     * { "foo" :
     *   [
     *     { "bar": "baz" },
     *     { "bar": "wibble" }
     *   ]
     * }
     * </pre>
     *
     * using the key {@code "foo.bar"} returns {@code [baz, wibble]}
     *
     * @param object the asset object
     * @param key a dot-separated path of fields
     * @return the value at the given path extracted from the asset
     */
    private static Object getValue(Map<?, ?> object, String key) {
        List<String> path = Arrays.asList(key.split("\\."));
        return getValue(object, path);
    }

    /**
     * As {@link #getValue(Map, String)} except accepting a list of path segments rather than a
     * dot-separated string of path segments.
     *
     * @param object the asset object
     * @param path the path
     * @return the value at the given path within the object
     */
    private static Object getValue(Map<?, ?> object, List<String> path) {
        Object nextObject = object.get(path.get(0));
        if (path.size() == 1) {
            return nextObject;
        } else if (nextObject instanceof Map) {
            return getValue((Map<?, ?>) nextObject, path.subList(1, path.size()));
        } else if (nextObject instanceof List) {
            List<Object> results = new ArrayList<Object>();
            for (Object listObj : (List<?>) nextObject) {
                if (listObj instanceof Map) {
                    results.add(getValue((Map<?, ?>) listObj, path.subList(1, path.size())));
                }
            }
            return results;
        } else {
            return null;
        }
    }

    /**
     * Sorts a list of asset objects accoring to the given SortOptions
     *
     * @param assets the assets
     * @param sort the sort options
     */
    private static void sort(List<Map<String, Object>> assets, SortOptions sort) {
        if (sort == null) {
            return;
        }
        Collections.sort(assets, new SortOptionComparator(sort));
    }

    /**
     * Paginates a list of objects according to the given PaginationOptions
     *
     * @param list the objects
     * @param options the pagination options
     * @return a sublist of objects, extracted using the pagination options
     */
    private static <T> List<T> paginate(List<T> list, PaginationOptions options) {
        if (options == null) {
            return list;
        }

        if (options.getOffset() >= list.size()) {
            return Collections.emptyList();
        }

        int limit = options.getLimit();
        int offset = options.getOffset();

        if (offset > list.size()) {
            return Collections.emptyList();
        }

        if (offset + limit > list.size()) {
            limit = list.size() - offset;
        }

        return list.subList(offset, offset + limit);
    }

    /**
     * A comparator for comparing assset objects based on SortOptions
     */
    private static class SortOptionComparator implements Comparator<Map<String, Object>> {
        private final SortOptions sort;

        public SortOptionComparator(SortOptions sort) {
            this.sort = sort;
        }

        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
            int result = ascendingCompare(o1, o2);
            if (sort.getSortOrder() == SortOrder.DESCENDING) {
                result = -result;
            }
            return result;
        }

        private int ascendingCompare(Map<String, Object> o1, Map<String, Object> o2) {
            Object v1 = getValue(o1, sort.getField());
            Object v2 = getValue(o2, sort.getField());
            String s1 = null;
            String s2 = null;
            if (v1 instanceof String) {
                s1 = (String) v1;
            }

            if (v2 instanceof String) {
                s2 = (String) v2;
            }

            if (s1 == null) {
                if (s2 == null) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (s2 == null) {
                return -1;
            } else {
                return s1.compareTo(s2);
            }
        }
    }

    @SuppressWarnings("unused")
    private static class AttachmentContent {
        private final String name;
        private final String contentType;
        private final String id;
        private final byte[] content;

        public AttachmentContent(String name, String contentType, String id, byte[] content) {
            this.name = name;
            this.contentType = contentType;
            this.id = id;
            // Note we do not defensively copy the content so you must not alter
            // the contents of the array
            this.content = content;
        }
    }

    /**
     * A parser for search strings
     *
     * The search string is tokenized and split into three buckets:
     * <ul>
     * <li>quoted phrases</li>
     * <li>unquoted words</li>
     * <li>negated words (words beginning with a - sign)</li>
     * <ul>
     *
     * <p>
     * E.g. the string {@code foo bar -baz "hello world"} would be split into the phrase
     * {@code hello world}, the words {@code foo} and {@code bar} and the negated word {@code baz}.
     */
    private static class SearchStringParser {
        private final List<String> phrases = new ArrayList<String>();
        private final List<String> words = new ArrayList<String>();
        private final List<String> negations = new ArrayList<String>();
        StringBuilder token = new StringBuilder();
        boolean quoted = false;
        boolean inToken = false;

        private SearchStringParser(String searchString) {
            for (int i = 0; i < searchString.length(); i++) {
                char c = searchString.charAt(i);
                if (c == '"') {
                    if (quoted) {
                        addChar(c);
                        endToken();
                    } else {
                        endToken();
                        quoted = true;
                        addChar(c);
                    }
                } else if (Character.isAlphabetic(c) || Character.isDigit(c) || c == '-') {
                    addChar(c);
                } else {
                    if (quoted) {
                        addChar(c);
                    } else {
                        endToken();
                    }
                }
            }

            endToken();
        }

        private void addChar(char c) {
            token.append(c);
            inToken = true;
        }

        private void endToken() {
            if (!inToken) {
                return;
            }

            String finalToken = token.toString();
            token = new StringBuilder();
            inToken = false;
            quoted = false;
            if (finalToken.startsWith("\"") && finalToken.endsWith("\"")) {
                finalToken = finalToken.substring(1, finalToken.length() - 1);
                phrases.add(finalToken);
            } else if (finalToken.startsWith("-")) {
                finalToken = finalToken.substring(1, finalToken.length());
                negations.add(finalToken);
            } else {
                words.add(finalToken);
            }
        }
    }

}
