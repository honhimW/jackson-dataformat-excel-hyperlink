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

package honhimw.jackson.dataformat.hyper.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;
import com.fasterxml.jackson.databind.util.Annotations;
import honhimw.jackson.dataformat.hyper.annotation.DataGrid;

import java.io.IOException;

public final class DataGridBeanDeserializer extends DelegatingDeserializer {

    public DataGridBeanDeserializer(final JsonDeserializer<?> d) {
        super(d);
    }

    @Override
    public Object deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        return super.deserialize(p, ctxt);
    }

    @Override
    protected JsonDeserializer<?> newDelegatingInstance(final JsonDeserializer<?> newDelegatee) {
        throw new IllegalStateException("Internal error: should never get called");
    }

    public static final class Modifier extends BeanDeserializerModifier {
        @Override
        public JsonDeserializer<?> modifyDeserializer(final DeserializationConfig config, final BeanDescription beanDesc, final JsonDeserializer<?> deserializer) {
            final Annotations annotations = beanDesc.getClassAnnotations();
            if (annotations.has(DataGrid.class)) {
                return new DataGridBeanDeserializer(deserializer);
            }
            return deserializer;
        }
    }
}
