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

package honhimw.jackson.dataformat.hyper.schema.generator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.UnsupportedTypeSerializer;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.fasterxml.jackson.databind.util.ClassUtil;
import honhimw.jackson.dataformat.hyper.ExcelDateModule;
import honhimw.jackson.dataformat.hyper.schema.Column;
import honhimw.jackson.dataformat.hyper.schema.ColumnPointer;
import honhimw.jackson.dataformat.hyper.annotation.DataColumn;
import honhimw.jackson.dataformat.hyper.annotation.DataGrid;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.stream.BaseStream;

@Slf4j
final class ObjectFormatVisitor extends JsonObjectFormatVisitor.Base {

    private final FormatVisitorWrapper _wrapper;

    ObjectFormatVisitor(final FormatVisitorWrapper wrapper, final SerializerProvider provider) {
        super(provider);
        _wrapper = wrapper;
    }

    @Override
    public void property(final BeanProperty prop) throws JsonMappingException {
        _columnProperty(prop);
    }

    @Override
    public void optionalProperty(final BeanProperty prop) throws JsonMappingException {
        _columnProperty(prop);
    }

    private void _columnProperty(final BeanProperty prop) throws JsonMappingException {
        final JavaType type = prop.getType();
        if (type.isTypeOrSubTypeOf(BaseStream.class)) {
            throw new IllegalStateException(type.getRawClass() + " is not (yet?) supported");
        }
        final ColumnPointer pointer = _wrapper.getPointer().resolve(prop.getName());
        final DataGrid.Value gridValue = _gridValue(prop);
        final DataColumn.Value columnValue = _columnValue(prop, gridValue);
        final FormatVisitorWrapper visitor = new FormatVisitorWrapper(pointer, gridValue, columnValue, _provider);
        final JsonSerializer<Object> serializer = _findValueSerializer(prop);
        _checkTypeSupported(serializer);
        serializer.acceptJsonFormatVisitor(visitor, type);
        if (visitor.isEmpty()) {
            final String columnName = _resolveColumnName(prop);
            _wrapper.add(new Column(pointer, columnValue.withName(columnName), type));
        } else {
            _wrapper.addAll(visitor);
        }
    }

    private JsonSerializer<Object> _findValueSerializer(final BeanProperty prop) throws JsonMappingException {
        if (prop instanceof BeanPropertyWriter) {
            final BeanPropertyWriter writer = (BeanPropertyWriter) prop;
            if (writer.hasSerializer()) {
                return writer.getSerializer();
            }
        }
        return _provider.findValueSerializer(prop.getType());
    }

    private String _resolveColumnName(final BeanProperty prop) {
        final ColumnNameResolver resolver = (ColumnNameResolver) _provider.getAttribute(ColumnNameResolver.class);
        return resolver.resolve(prop);
    }

    private DataGrid.Value _gridValue(BeanProperty prop) {
        final DataGrid ann = prop.getContextAnnotation(DataGrid.class);
        return DataGrid.Value.from(ann).withDefaults(_wrapper.getSheet());
    }

    private DataColumn.Value _columnValue(BeanProperty prop, DataGrid.Value grid) {
        final DataColumn ann = prop.getAnnotation(DataColumn.class);
        return DataColumn.Value.from(ann).withDefaults(grid);
    }

    private void _checkTypeSupported(final JsonSerializer<Object> serializer) throws JsonMappingException {
        if (serializer instanceof UnsupportedTypeSerializer) {
            try {
                serializer.serialize(null, null, _provider);
            } catch (InvalidDefinitionException e) {
                _reportBadDefinition(e);
            } catch (IOException e) {
                throw JsonMappingException.fromUnexpectedIOE(e);
            }
        }
    }

    private void _reportBadDefinition(final InvalidDefinitionException e) throws InvalidDefinitionException {
        if (!BeanUtil.isJava8TimeClass(e.getType().getRawClass())) throw e;
        if (log.isTraceEnabled()) log.trace(e.getMessage());
        final String msg = "Java 8 date/time type " + ClassUtil.getTypeDescription(e.getType())
                + " not supported by default: register Module `" + ExcelDateModule.class.getName()
                + "` or add Module \"com.fasterxml.jackson.datatype:jackson-datatype-jsr310\" to enable handling";
        throw InvalidDefinitionException.from((JsonGenerator) e.getProcessor(), msg, e.getType());
    }
}
