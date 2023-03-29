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

package io.github.honhimw.jackson.dataformat.hyper.schema;

import com.fasterxml.jackson.core.FormatSchema;
import io.github.honhimw.jackson.dataformat.hyper.schema.visitor.BookReadVisitor;
import io.github.honhimw.jackson.dataformat.hyper.schema.visitor.BookWriteVisitor;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.poi.ss.util.CellAddress;

public final class HyperSchema implements FormatSchema, Iterable<Column> {

    public static final String SCHEMA_TYPE = "hyper";
    private final List<Column> _columns;
    private final List<Table> _tables;
    private final CellAddress _origin;
    private final Function<BookWriteVisitor, BookWriteVisitor> _bookWriteVisitor;
    private final Function<BookReadVisitor, BookReadVisitor> _bookReadVisitor;

    public HyperSchema(final List<Column> _columns, final List<Table> _tables, final CellAddress _origin,
        final Function<BookWriteVisitor, BookWriteVisitor> bookWriteVisitor,
        final Function<BookReadVisitor, BookReadVisitor> bookReadVisitor) {
        this._columns = _columns;
        this._tables = _tables;
        this._origin = _origin;
        this._bookWriteVisitor = bookWriteVisitor;
        this._bookReadVisitor = bookReadVisitor;
        setupTables();
    }

    @Override
    public String getSchemaType() {
        return SCHEMA_TYPE;
    }

    @Override
    public Iterator<Column> iterator() {
        return _columns.iterator();
    }

    public Function<BookWriteVisitor, BookWriteVisitor> getBookWriteVisitor() {
        return _bookWriteVisitor;
    }

    public Function<BookReadVisitor, BookReadVisitor> getBookReadVisitor() {
        return _bookReadVisitor;
    }

    public Column findColumn(final CellAddress reference) {
        if (_columns.isEmpty()) {
            return null;
        }
        return getColumn(reference);
    }

    public Column getColumn(final String tableName, final CellAddress reference) {
        Table table = getTable(tableName);
        return table.getColumns().get(reference.getColumn() - getOriginColumn());
    }

    public Column getColumn(final Class<?> clazz, final CellAddress reference) {
        Table table = getTable(clazz);
        return table.getColumns().get(reference.getColumn() - getOriginColumn());
    }

    public Column getColumn(final CellAddress reference) {
        return _columns.get(reference.getColumn() - getOriginColumn());
    }

    public List<Table> getTables() {
        return _tables;
    }

    public int getDataRow() {
        return _origin.getRow() + 1;
    }

    public int getOriginColumn() {
        return _origin.getColumn();
    }

    public int columnIndexOfCurrentSheet(final Class<?> clazz, String name) {
        Table table = _tables.stream().filter(t -> t.getType().getRawClass().equals(clazz))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("sheet table is not exists"));
        List<Column> tableColumns = table.getColumns();
        ColumnPointer tablePointer = table.getPointer();
        ColumnPointer pointer = tablePointer.resolve(name);
        for (int i = 0; i < tableColumns.size(); i++) {
            if (tableColumns.get(i).matches(pointer)) {
                return i + getOriginColumn();
            }
        }
        return -1;
    }

    public int columnIndexOfCurrentSheet(final ColumnPointer pointer) {
        Table table = currentTable(pointer);
        List<Column> tableColumns = table.getColumns();
        for (int i = 0; i < tableColumns.size(); i++) {
            if (tableColumns.get(i).matches(pointer)) {
                return i + getOriginColumn();
            }
        }
        return -1;
    }

    public Table getTable(final String name) {
        return _tables.stream().filter(t -> t.getName().equals(name))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("sheet table " + name + " is not exists"));
    }

    public Table getTable(final Class<?> clazz) {
        return _tables.stream().filter(t -> t.getType().getRawClass().equals(clazz))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("sheet table " + clazz.getSimpleName() + " is not exists"));
    }

    public Table currentTable(final ColumnPointer pointer) {
        return _tables.stream().filter(t -> t.matches(pointer.getParent()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("sheet " + pointer + " table is not exists"));
    }

    public int columnIndexOf(final ColumnPointer pointer) {
        for (int i = 0; i < _columns.size(); i++) {
            if (_columns.get(i).matches(pointer)) {
                return i + getOriginColumn();
            }
        }
        return -1;
    }

    public int columnIndexOf(final Column column) {
        return columnIndexOf(column.getPointer());
    }

    public int getOriginRow() {
        return _origin.getRow();
    }

    public List<Column> getColumns(final ColumnPointer filter) {
        if (filter.isEmpty()) {
            return _columns;
        }
        return _columns.stream().filter(c -> c.getPointer().startsWith(filter)).collect(Collectors.toList());
    }

    public boolean isInRowBounds(final int row) {
        return getDataRow() <= row;
    }

    public boolean isInColumnBounds(final int col) {
//        return getOriginColumn() <= col && col < getOriginColumn() + _columns.size();
        return getOriginColumn() <= col;
    }

    private void setupTables() {
        for (final Table table : _tables) {
            for (final Column column : _columns) {
                if (table.matches(column.getPointer().getParent()) && !table.getPointer().equals(column.getPointer())) {
                    table.getColumns().add(column);
                    column.setTable(table);
                }
            }
            table.setOrigin(_origin);
        }
    }

    private void checkType() {
        for (final Column column : this) {
            if (column.getType().isMapLikeType()) {
                throw new IllegalArgumentException("map like types are not supported");
            }
        }
    }
}
