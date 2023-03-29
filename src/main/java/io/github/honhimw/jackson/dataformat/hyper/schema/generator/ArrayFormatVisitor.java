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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import io.github.honhimw.jackson.dataformat.hyper.schema.Column;
import io.github.honhimw.jackson.dataformat.hyper.schema.ColumnPointer;

final class ArrayFormatVisitor extends JsonArrayFormatVisitor.Base {

    private final FormatVisitorWrapper _wrapper;
    private final JavaType _type;

    ArrayFormatVisitor(final FormatVisitorWrapper wrapper, final JavaType type, final SerializerProvider provider) {
        super(provider);
        _wrapper = wrapper;
        _type = type;
    }

    @Override
    public void itemsFormat(final JsonFormatVisitable handler, final JavaType elementType) throws JsonMappingException {
        final ColumnPointer pointer = _wrapper.getPointer().resolveArray();
        final FormatVisitorWrapper visitor = new FormatVisitorWrapper(_wrapper, pointer);
        handler.acceptJsonFormatVisitor(visitor, elementType);
        if (visitor.isEmpty()) {
            _wrapper.add(new Column(pointer, _wrapper.getColumnName(), _type, null));
        } else {
            _wrapper.addAll(visitor);
        }
    }

    @Override
    public void itemsFormat(final JsonFormatTypes format) throws JsonMappingException {
        final ColumnPointer pointer = _wrapper.getPointer().resolveArray();
        _wrapper.add(new Column(pointer, _wrapper.getColumnName(), _type, null));
    }
}
