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

package io.jigson.core.flow;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.jigson.config.Context;
import io.jigson.json.pipe.JsonPipe;
import io.jigson.pipe.UnitaryFlow;

import java.util.Objects;
import java.util.stream.IntStream;

import static io.jigson.utils.CriterionUtils.findCriterion;
import static io.jigson.utils.CriterionUtils.isCriterion;
import static io.jigson.utils.JsonUtils.getPropertyByPath;
import static io.jigson.utils.PathUtils.findPropertyName;
import static io.jigson.utils.PathUtils.isFunction;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class FetchFlow implements UnitaryFlow<JsonElement> {

    private final Query query;
    private final Context context;

    public FetchFlow(final Query query, final Context context) {
        this.query = query.fork();
        this.context = context;
    }

    public JsonElement flow(final JsonElement jsonElement) {

        JsonElement currentElement = jsonElement;
        String currentPath = query.nextPath();

        while (isNotBlank(currentPath)) {
            if (isFunction(currentPath)) {
                currentElement = FunctionFlowDispatcher.dispatch(currentElement, currentPath, context);
            } else {
                currentElement = fetch(currentElement, currentPath);
            }
            currentPath = query.nextPath();
        }
        return currentElement;
    }

    private JsonElement fetch(final JsonElement currentElement, final String currentPath) {
        if (Objects.isNull(currentElement) || currentElement.isJsonNull()) {
            return JsonNull.INSTANCE;
        } else if (currentElement.isJsonObject()) {
            return fetchObject(currentElement.getAsJsonObject(), currentPath);
        } else if (currentElement.isJsonArray()) {
            return fetchArray(currentElement.getAsJsonArray(), currentPath);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private JsonElement fetchObject(final JsonObject jsonObject, final String currentPath) {
        final JsonElement currentElement = getPropertyByPath(jsonObject, currentPath);
        final String criterion = findCriterion(currentPath);
        if (isCriterion(criterion)) {
            return JsonPipe.from(currentElement).withContext(context).filter(criterion).get().orElse(JsonNull.INSTANCE);
        }
        return currentElement;
    }

    private JsonElement fetchArray(final JsonArray jsonArray, final String currentPath) {

        final String propertyName = findPropertyName(currentPath);
        final String criterion = findCriterion(currentPath);

        final JsonArray accumulator = new JsonArray();

        IntStream.range(0, jsonArray.size())
                .mapToObj(jsonArray::get)
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .map(o -> fetchObject(o, propertyName))
                .forEach(accumulator::add);

        if (isCriterion(criterion)) {
            return JsonPipe.from(accumulator).withContext(context).filter(criterion).get().orElse(JsonNull.INSTANCE);
        }
        return accumulator;
    }
}
