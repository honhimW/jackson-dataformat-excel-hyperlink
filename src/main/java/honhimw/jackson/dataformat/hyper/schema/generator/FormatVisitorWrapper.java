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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonMapFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import honhimw.jackson.dataformat.hyper.schema.Column;
import honhimw.jackson.dataformat.hyper.schema.ColumnPointer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
public final class FormatVisitorWrapper extends JsonFormatVisitorWrapper.Base implements Iterable<Column> {

    private final ColumnPointer _pointer;
    private final String _columnName;
    private final List<Column> _columns;

    public FormatVisitorWrapper() {
        this(ColumnPointer.empty(), "", null);
    }

    FormatVisitorWrapper(final FormatVisitorWrapper base, final ColumnPointer pointer) {
        this(pointer, base._columnName, base._provider);
    }

    FormatVisitorWrapper(final ColumnPointer pointer, final String columnName, final SerializerProvider provider) {
        super(provider);
        _pointer = pointer;
        _columnName = columnName;
        _columns = new ArrayList<>();
    }

    @Override
    public JsonObjectFormatVisitor expectObjectFormat(final JavaType type) throws JsonMappingException {
//        add(new Column(_pointer, "", type, false));
        return new ObjectFormatVisitor(this, type, _provider);
    }

    @Override
    public JsonArrayFormatVisitor expectArrayFormat(final JavaType type) throws JsonMappingException {
        return new ArrayFormatVisitor(this, type, _provider);
    }

    @Override
    public JsonMapFormatVisitor expectMapFormat(final JavaType type) throws JsonMappingException {
        throw JsonMappingException.from((SerializerProvider) null, "Unsupported type: " + type);
    }

    @Override
    public Iterator<Column> iterator() {
        return _columns.iterator();
    }

    ColumnPointer getPointer() {
        return _pointer;
    }

    String getColumnName() {
        return _columnName;
    }

    boolean isEmpty() {
        return _columns.isEmpty();
    }

    void add(final Column column) {
        _columns.add(column);
    }

    void addAll(final Iterable<Column> columns) {
        for (final Column column : columns) {
            add(column);
        }
    }
}
