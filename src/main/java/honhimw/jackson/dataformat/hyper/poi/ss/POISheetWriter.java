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

package honhimw.jackson.dataformat.hyper.poi.ss;

import honhimw.jackson.dataformat.hyper.schema.Column;
import honhimw.jackson.dataformat.hyper.schema.ColumnPointer;
import honhimw.jackson.dataformat.hyper.schema.SpreadsheetSchema;
import honhimw.jackson.dataformat.hyper.ser.SheetWriter;
import honhimw.jackson.dataformat.hyper.annotation.DataColumn;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Date1904Support;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.SheetUtil;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.function.BiConsumer;

@Slf4j
public final class POISheetWriter implements SheetWriter {

    private static final int MAX_COLUMN_WIDTH = 255 * 256;

    private final Sheet _sheet;
    private SpreadsheetSchema _schema;
    private CellAddress _reference;
    private int _lastRow;

    public POISheetWriter(final Sheet sheet) {
        _sheet = sheet;
    }

    @Override
    public SpreadsheetVersion getSpreadsheetVersion() {
        return _sheet.getWorkbook().getSpreadsheetVersion();
    }

    @Override
    public void setSchema(final SpreadsheetSchema schema) {
        _schema = schema;
    }

    @Override
    public void setReference(final CellAddress reference) {
        _reference = reference;
    }

    @Override
    public void writeHeaders() {
        final int row = _schema.getOriginRow();
        for (final Column column : _schema) {
            final int col = _schema.columnIndexOf(column);
            if (_sheet instanceof SXSSFSheet && column.getValue().isAutoSize()) {
                ((SXSSFSheet) _sheet).trackColumnForAutoSizing(col);
            }
            setReference(new CellAddress(row, col));
            writeString(column.getName());
        }
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
        final int row = _reference.getRow();
        final Cell cell = CellUtil.getCell(CellUtil.getRow(row, _sheet), _reference.getColumn());
        consumer.accept(cell, value);
        final Column column = _schema.findColumn(_reference);
        _lastRow = Math.max(_lastRow, row);
        if (log.isTraceEnabled()) {
            log.trace("{} {} {}", _reference, cell.getCellType(), cell);
        }
    }

    @Override
    public void mergeScopedColumns(final ColumnPointer filter, final int row, final int size) {
        if (size <= 1) return;
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
        for (final Column column : _schema) {
            final int col = _schema.columnIndexOf(column);
            final DataColumn.Value value = column.getValue();
            double width;
            if (value.isAutoSize()) {
                if (_sheet instanceof SXSSFSheet) {
                    _sheet.autoSizeColumn(col, true);
                    width = _sheet.getColumnWidth(col) / 256d;
                } else {
                    int firstRow = Math.max(_sheet.getFirstRowNum(), _lastRow - 100);
                    width = SheetUtil.getColumnWidth(_sheet, col, true, firstRow, _lastRow);
                }
                width = Math.max(width, value.getMinWidth());
                width = Math.min(width, value.getMaxWidth());
            } else {
                width = value.getWidth();
            }
            if (width > 0) {
                width *= 256;
                width = Math.min(width, MAX_COLUMN_WIDTH);
                _sheet.setColumnWidth(col, (int) width);
            }
        }
    }

    @Override
    public void close() throws IOException {
        final Workbook workbook = _sheet.getWorkbook();
        workbook.close();
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
