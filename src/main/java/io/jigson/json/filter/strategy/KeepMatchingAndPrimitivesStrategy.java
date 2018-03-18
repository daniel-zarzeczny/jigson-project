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

package io.jigson.json.filter.strategy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import io.jigson.config.Context;

import java.util.stream.IntStream;

import static io.jigson.utils.StreamUtils.not;

class KeepMatchingAndPrimitivesStrategy extends AbstractJsonArrayFilterStrategy {

    static final KeepMatchingAndPrimitivesStrategy INSTANCE = new KeepMatchingAndPrimitivesStrategy();

    private KeepMatchingAndPrimitivesStrategy() {
    }

    @Override
    public JsonElement filter(final JsonArray jsonArray, final String criterion, final Context context) {
        final JsonArray filteredArray = new JsonArray();
        IntStream
                .range(STARTING_INDEX, jsonArray.size())
                .mapToObj(jsonArray::get)
                .map(item -> filterWithRouting(item, criterion, context))
                .filter(not(JsonElement::isJsonNull))
                .forEach(filteredArray::add);
        return filteredArray.size() > 0 ? filteredArray : JsonNull.INSTANCE;
    }
}
