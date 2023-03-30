/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.honhimw.jackson.dataformat.hyper.schema.generator;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.BeanProperty;

public interface ColumnNameResolver {

    ColumnNameResolver DEFAULT = prop -> {
        JsonPropertyDescription annotation = prop.getAnnotation(JsonPropertyDescription.class);
        if (annotation != null) {
            String value = annotation.value();
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return prop.getName();
    };

    String resolve(BeanProperty prop);
}
