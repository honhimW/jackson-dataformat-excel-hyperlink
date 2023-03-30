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

package io.github.honhimw.jackson.dataformat.hyper.poi.ss;

import static io.github.honhimw.jackson.dataformat.hyper.poi.RetainedSheets.LIST;

import com.fasterxml.jackson.databind.JavaType;
import io.github.honhimw.jackson.dataformat.hyper.poi.RetainedSheets;
import io.github.honhimw.jackson.dataformat.hyper.schema.Column;
import io.github.honhimw.jackson.dataformat.hyper.schema.ColumnPointer;
import io.github.honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import io.github.honhimw.jackson.dataformat.hyper.schema.Table;
import io.github.honhimw.jackson.dataformat.hyper.schema.visitor.BookWriteVisitor;
import io.github.honhimw.jackson.dataformat.hyper.schema.visitor.RowWriteVisitor;
import io.github.honhimw.jackson.dataformat.hyper.schema.visitor.SheetWriteVisitor;
import io.github.honhimw.jackson.dataformat.hyper.ser.BookWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Date1904Support;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

@Slf4j
public final class POIBookWriter implements BookWriter {

    private final Workbook _workbook;
    private final Map<Class<?>, Sheet> _sheetMap = new HashMap<>();
    private Sheet _sheet;
    private HyperSchema _schema;
    private CellAddress _reference;

    private BookWriteVisitor _bookWriteVisitor;
    private SheetWriteVisitor _sheetWriteVisitor;
    private RowWriteVisitor _rowWriteVisitor;

    public POIBookWriter(final Workbook _workbook) {
        this._workbook = _workbook;
        this._bookWriteVisitor = new POIBookWriteVisitor();
    }

    @Override
    public SpreadsheetVersion getSpreadsheetVersion() {
        return _workbook.getSpreadsheetVersion();
    }

    @Override
    public void setSchema(final HyperSchema schema) {
        this._schema = schema;
        Function<BookWriteVisitor, BookWriteVisitor> bookWriteVisitor = _schema.getBookWriteVisitor();
        if (bookWriteVisitor != null) {
            this._bookWriteVisitor = bookWriteVisitor.apply(new POIBookWriteVisitor());
        }
        this._bookWriteVisitor.visitBook(_workbook, _schema);
    }

    @Override
    public void switchSheet(final Class<?> type) {
        this._sheet = _sheetMap.get(type);
        Table table = null;
        if (!RetainedSheets.isRetain(type)) {
            table = _schema.getTable(type);
        }
        this._sheetWriteVisitor = _bookWriteVisitor.visitSheet(_sheet, table);
    }

    @Override
    public void setReference(final CellAddress reference) {
        this._reference = reference;
    }

    @Override
    public void currentValue(final Object value) {
        if (_sheet != null && _reference.getColumn() == _schema.getOriginColumn() && _schema.isInRowBounds(
            _reference.getRow())) {
            _sheetWriteVisitor.visitRow(CellUtil.getRow(_reference.getRow(), _sheet), value);
        }
    }

    @Override
    public Cell getCell() {
        final int row = _reference.getRow();
        return CellUtil.getCell(CellUtil.getRow(row, _sheet), _reference.getColumn());
    }

    @Override
    public void writeHeaders() {
        for (final Table table : _schema.getTables()) {
            writeHeader(table);
        }
        for (final Column column : _schema) {
            if (column.isArray()) {
                _sheetMap.put(List.class, _workbook.createSheet(LIST));
                break;
            }
        }
    }

    private void writeHeader(Table table) {
        final int row = _schema.getOriginRow();
        JavaType type = table.getType();
        Class<?> clazz = type.getRawClass();
        if (!_sheetMap.containsKey(clazz)) {
            String sheetName = table.getName();
            RetainedSheets.assertUsable(sheetName);
            Sheet sheet = _workbook.createSheet(sheetName);
            _sheetMap.put(clazz, sheet);
        }
        switchSheet(clazz);
        _rowWriteVisitor = _sheetWriteVisitor.visitHeaders(CellUtil.getRow(row, _sheet));
        List<Column> columns = table.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            final int col = _schema.getOriginColumn() + i;
            setReference(new CellAddress(row, col));
            Cell cell = getCell();
            _rowWriteVisitor.visitHeader(cell, column);
        }
    }

    @Override
    public void link(final Class<?> type, String value, int row) {
        Sheet sheet = _sheetMap.get(type);
        _write(value, (cell, s) -> {
            String address = String.format("#%s!%d:%d", sheet.getSheetName(), row, row);
            String text = StringUtil.isNotBlank(s) ? s : address;
            CreationHelper creationHelper = _workbook.getCreationHelper();
            Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.DOCUMENT);
            hyperlink.setAddress(address);
            cell.setHyperlink(hyperlink);
            cell.setCellValue(text);
        });
    }

    @Override
    public void writeNumeric(final double value) {
        _write(value, Cell::setCellValue);
    }

    @Override
    public void writeString(final String value) {
        _write(value, Cell::setCellValue);
    }

    @Override
    public void writeBoolean(final boolean value) {
        _write(value, Cell::setCellValue);
    }

    @Override
    public void writeBlank() {
        _write(null, (cell, o) -> cell.setBlank());
    }

    private <T> void _write(final T value, final BiConsumer<Cell, T> consumer) {
        final Cell cell = getCell();
        Column column = null;
        if (!RetainedSheets.isRetain(_sheet.getSheetName())) {
            column = _schema.getColumn(_sheet.getSheetName(), _reference);
        }
        _rowWriteVisitor.visitCell(cell, column, value, consumer);
        if (log.isTraceEnabled()) {
            log.trace("{} {} {}", _reference, cell.getCellType(), cell);
        }
    }

    @Override
    public void mergeScopedColumns(final ColumnPointer filter, final int row, final int size) {
        if (size <= 1) {
            return;
        }
        final List<Column> columns = _schema.getColumns(filter);
        for (final Column column : columns) {
            int col = _schema.columnIndexOf(column);
            if (!filter.relativize(column.getPointer()).contains(ColumnPointer.array())) {
                final CellRangeAddress region = new CellRangeAddress(row, row + size - 1, col, col);
                if (log.isTraceEnabled()) {
                    log.trace(region.formatAsString());
                }
                _sheet.addMergedRegion(region);
            }
        }
    }

    @Override
    public void write(final OutputStream out) throws IOException {
        _sheet.getWorkbook().write(out);
    }

    @Override
    public void adjustColumnWidth() {
    }

    @Override
    public void close() throws IOException {
        final Workbook workbook = _sheet.getWorkbook();
        workbook.close();
        _bookWriteVisitor.visitEnd();
        if (workbook instanceof SXSSFWorkbook) {
            ((SXSSFWorkbook) workbook).dispose();
        }
    }

    @Override
    public boolean isDate1904() {
        final Workbook workbook = _sheet.getWorkbook();
        if (workbook instanceof Date1904Support) {
            return ((Date1904Support) workbook).isDate1904();
        }
        return false;
    }
}
