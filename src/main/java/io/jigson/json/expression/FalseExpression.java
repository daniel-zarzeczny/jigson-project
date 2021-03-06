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

package io.jigson.json.expression;

import com.google.gson.JsonNull;

public class FalseExpression extends JsonExpression {

    static final FalseExpression INSTANCE = new FalseExpression();
    private static final String ALWAYS_FALSE_CRITERION = "1=0";

    private FalseExpression() {
        super(JsonNull.INSTANCE, ALWAYS_FALSE_CRITERION);
    }

    @Override
    public Boolean interpret() {
        return false;
    }
}
