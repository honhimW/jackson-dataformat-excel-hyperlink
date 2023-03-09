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
import honhimw.jackson.dataformat.hyper.annotation.DataColumn;
import honhimw.jackson.dataformat.hyper.annotation.DataGrid;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public final class FormatVisitorWrapper extends JsonFormatVisitorWrapper.Base implements Iterable<Column> {

    private final ColumnPointer _pointer;
    private final DataGrid.Value _grid;
    private final DataColumn.Value _column;
    private final List<Column> _columns;

    public FormatVisitorWrapper() {
        this(ColumnPointer.empty(), DataGrid.Value.empty(), DataColumn.Value.empty(), null);
    }

    FormatVisitorWrapper(final FormatVisitorWrapper base, final ColumnPointer pointer) {
        this(pointer, base._grid, base._column, base._provider);
    }

    FormatVisitorWrapper(final ColumnPointer pointer, final DataGrid.Value sheet, final DataColumn.Value column, final SerializerProvider provider) {
        super(provider);
        _pointer = pointer;
        _grid = sheet;
        _column = column;
        _columns = new ArrayList<>();
    }

    @Override
    public JsonObjectFormatVisitor expectObjectFormat(final JavaType type) throws JsonMappingException {
        return new ObjectFormatVisitor(this, _provider);
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

    DataGrid.Value getSheet() {
        return _grid;
    }

    DataColumn.Value getColumn() {
        return _column;
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
