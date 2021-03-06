/*
 *    Copyright 2018 Daniel Zarzeczny
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.jigson.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.jigson.core.flow.ExpressionFlow;
import io.jigson.core.flow.FetchFlow;
import io.jigson.core.flow.KeepFlow;
import io.jigson.core.flow.Query;
import io.jigson.json.pipe.JsonPipe;
import io.jigson.pipe.ContextJoinPipe;
import io.jigson.pipe.JigsonContext;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class Jigson {

    private final JsonElement jsonElement;
    private final PluginsConfig pluginsConfig = new PluginsConfig(this);

    private Jigson(final JsonElement jsonElement) {
        this.jsonElement = jsonElement;
        this.pluginsConfig.registerEmbeddedPlugins();
        JigsonConfigHolder.init();
    }

    public static Jigson from(final JsonElement jsonElement) {
        return new Jigson(jsonElement);
    }

    public Jigson withConfig(final JigsonConfig config) {
        JigsonConfigHolder.set(config);
        return this;
    }

    public PluginsConfig pluginsConfig() {
        return pluginsConfig;
    }

    public JsonElement parse(final String rawQuery) {
        return parse(rawQuery, JigsonContext.newContext());
    }

    public JsonElement parse(final String rawQuery, final JigsonContext context) {

        final String trimmedQuery = Optional.ofNullable(rawQuery).orElse(EMPTY).trim();

        if (isNotBlank(trimmedQuery)) {

            final Query query = Query.from(trimmedQuery);
            final Token initialToken = query.next();
            final int initialTokenIndex = initialToken.getIndex();

            if (Token.AT_SYMBOL == initialTokenIndex) {
                return fetch(query, context);
            } else if (Token.HASH_SYMBOL == initialTokenIndex) {
                return keep(query);
            } else if (Token.DOLLAR_SYMBOL == initialTokenIndex) {
                return expression(trimmedQuery, context);
            }
            throw new IllegalPrefixTokenException();
        }
        throw new UnexpectedSymbolException();
    }

    private JsonElement fetch(final Query query, final JigsonContext context) {
        final FetchFlow flow = new FetchFlow(query);
        return ContextJoinPipe.from(jsonElement).withContext(context).flush(flow).get();
    }

    private JsonElement keep(final Query query) {
        return new KeepFlow(query).flow(jsonElement);
    }

    private JsonPrimitive expression(final String query, final JigsonContext context) {
        final ExpressionFlow flow = new ExpressionFlow(query);
        return ContextJoinPipe.from(jsonElement).withContext(context).flush(flow).get();
    }

    public JsonPipe parseThen(final String rawQuery) {
        return parseThen(rawQuery, JigsonContext.newContext());
    }

    public JsonPipe parseThen(final String rawQuery, final JigsonContext context) {
        final JsonElement result = parse(rawQuery, context);
        return JsonPipe.from(result);
    }

}
