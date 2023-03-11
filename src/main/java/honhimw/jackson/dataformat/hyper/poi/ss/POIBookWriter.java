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

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.databind.JavaType;
import honhimw.jackson.dataformat.hyper.schema.Column;
import honhimw.jackson.dataformat.hyper.schema.ColumnPointer;
import honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import honhimw.jackson.dataformat.hyper.schema.Table;
import honhimw.jackson.dataformat.hyper.ser.BookWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
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

    private static final int MAX_COLUMN_WIDTH = 255 * 256;

    private static final Set<String> RETAIN_SHEET_NAMES = Set.of("List", "Set", "Map", "Object");

    private final Workbook _workbook;
    private final Map<Class<?>, Sheet> _sheetMap = new HashMap<>();
    private Sheet _sheet;
    private HyperSchema _schema;
    private CellAddress _reference;
    private int _lastRow;

    public POIBookWriter(final Workbook _workbook) {
        this._workbook = _workbook;
    }

    @Override
    public SpreadsheetVersion getSpreadsheetVersion() {
        return _workbook.getSpreadsheetVersion();
    }

    @Override
    public void switchSheet(final Class<?> type) {
        this._sheet = _sheetMap.get(type);
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
    public void setSchema(final HyperSchema schema) {
        _schema = schema;
    }

    @Override
    public void setReference(final CellAddress reference) {
        _reference = reference;
    }

    @Override
    public void writeHeaders() {
        final int row = _schema.getOriginRow();
        for (final Table table : _schema.getTables()) {
            JavaType type = table.getType();
            Class<?> clazz = type.getRawClass();
            String sheetName = null;
            if (!_sheetMap.containsKey(clazz)) {
                if (clazz.isAnnotationPresent(JsonClassDescription.class)) {
                    JsonClassDescription annotation = clazz.getAnnotation(JsonClassDescription.class);
                    sheetName = annotation.value();
                }
                if (StringUtil.isBlank(sheetName)) {
                    sheetName = clazz.getSimpleName();
                }
                if (RETAIN_SHEET_NAMES.contains(sheetName)) {
                    throw new IllegalArgumentException(String.format("[%s] is retained, please rename the sheet", sheetName));
                }
                Sheet sheet = _workbook.createSheet(sheetName);
                _sheetMap.put(clazz, sheet);
                this._sheet = sheet;
            } else {
                this._sheet = _sheetMap.get(clazz);
            }
            List<Column> columns = table.getColumns();
            for (int i = 0; i < columns.size(); i++) {
                final int col = _schema.getOriginColumn() + i;
                setReference(new CellAddress(row, col));
                writeString(columns.get(i).getName());
            }
        }
        for (final Column column : _schema) {
            if (column.isArray()) {
                _sheetMap.put(List.class, _workbook.createSheet("List"));
                break;
            }
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

    private <T> void  _write(final T value, final BiConsumer<Cell, T> consumer) {
        final int row = _reference.getRow();
        final Cell cell = CellUtil.getCell(CellUtil.getRow(row, _sheet), _reference.getColumn());
        consumer.accept(cell, value);
        _lastRow = Math.max(_lastRow, row);
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
