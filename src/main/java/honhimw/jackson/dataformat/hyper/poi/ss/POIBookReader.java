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

import honhimw.jackson.dataformat.hyper.deser.CellValue;
import honhimw.jackson.dataformat.hyper.deser.BookReader;
import honhimw.jackson.dataformat.hyper.deser.SheetToken;
import honhimw.jackson.dataformat.hyper.poi.RetainSheetNames;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

@Slf4j
public final class POIBookReader implements BookReader {

    public final static Pattern pattern = Pattern.compile("#(?<sheet>.*)!(?<first>\\d+):(?<last>\\d+)");

    private final Workbook _workbook;
    private final DataFormatter _formatter = new DataFormatter();
    private final Map<String, Sheet> _sheetMap = new HashMap<>();
    private final Sheet _mainSheet;
    private final Iterator<Row> _mainRowIterator;
    private Iterator<Cell> _cellIterator;
    private SheetToken _next;
    private final Stack<Iterator<Cell>> _cellIteratorStack = new Stack<>();
    private Cell _cell;
    private int _rowIndex = -1;
    private int _columnIndex = -1;
    private boolean _closed;

    public POIBookReader(final Workbook workbook) {
        _workbook = workbook;
        _next = SheetToken.SHEET_DATA_START;
        _mainSheet = _workbook.getSheetAt(0);
        _workbook.sheetIterator().forEachRemaining(rows -> _sheetMap.put(rows.getSheetName(), rows));
        _mainRowIterator = _mainSheet.rowIterator();
    }

    @Override
    public SpreadsheetVersion getSpreadsheetVersion() {
        return _workbook.getSpreadsheetVersion();
    }

    @Override
    public boolean isDate1904() {
        if (_workbook instanceof Date1904Support date1904Support) {
            return date1904Support.isDate1904();
        }
        return false;
    }

    @Override
    public CellAddress getReference() {
        return _cell == null ? null : _cell.getAddress();
    }

    @Override
    public honhimw.jackson.dataformat.hyper.deser.CellValue getCellValue() {
        if (_cell == null) return null;
        final CellType type = CellFormat.ultimateType(_cell);
        switch (type) {
            case NUMERIC:
                final double value = _cell.getNumericCellValue();
                return new honhimw.jackson.dataformat.hyper.deser.CellValue(value, _formattedString(value, _cell.getCellStyle()));
            case STRING:
                return new honhimw.jackson.dataformat.hyper.deser.CellValue(_cell.getStringCellValue());
            case BOOLEAN:
                return honhimw.jackson.dataformat.hyper.deser.CellValue.valueOf(_cell.getBooleanCellValue());
            case ERROR:
                return CellValue.getError(_cell.getErrorCellValue());
            case BLANK:
                return null;
            case _NONE:
            case FORMULA:
        }
        throw new IllegalStateException("Unexpected cell value type: " + type);
    }

    @Override
    public Cell getCell() {
        return _cell;
    }

    @Override
    public int getRow() {
        return _rowIndex;
    }

    @Override
    public int getColumn() {
        return _columnIndex;
    }

    @Override
    public boolean isClosed() {
        return _closed;
    }

    @Override
    public void close() throws IOException {
        _workbook.close();
        _closed = true;
    }

    @Override
    public boolean hasNext() {
        return _next != null;
    }

    @Override
    public SheetToken next() {
        if (_next == null) throw new NoSuchElementException();
        final SheetToken token = _next;
        switch (token) {
            case SHEET_DATA_START -> {
                _next = _mainRowIterator.hasNext() ? SheetToken.ROW_START : SheetToken.SHEET_DATA_END;
            }
            case ROW_START -> {
                final Row row = _mainRowIterator.next();
                _rowIndex = row.getRowNum();
                _cellIterator = row.cellIterator();
                _next = _cellIterator.hasNext() ? SheetToken.CELL_VALUE : SheetToken.ROW_END;
            }
            case CELL_VALUE -> {
                _cell = _cellIterator.next();
                _columnIndex = _cell.getColumnIndex();
                _next = _cellIterator.hasNext() ? SheetToken.CELL_VALUE : SheetToken.ROW_END;
                Hyperlink hyperlink = _cell.getHyperlink();
                if (Objects.nonNull(hyperlink)) {
                    Matcher matcher = pattern.matcher(hyperlink.getAddress());
                    if (matcher.find()) {
                        String label = matcher.group("sheet");
                        String first = matcher.group("first");
                        int firstRow = Integer.parseInt(first);
                        Row linkedRow = _sheetMap.get(label).getRow(firstRow - 1);
                        Iterator<Cell> cellIterator = linkedRow.cellIterator();
                        if (cellIterator.hasNext()) {
                            _cellIteratorStack.push(_cellIterator);
                            _cellIterator = cellIterator;
                            _next = SheetToken.CELL_VALUE;
                        } else {
                            _next = SheetToken.ROW_END;
                        }
                    }

                }
            }
            case HYPER_LINK -> {

            }
            case ROW_END -> {
                _cell = null;
                _cellIterator = null;
                if (!_cellIteratorStack.isEmpty()) {
                    _cellIterator = _cellIteratorStack.pop();
                }
                if (Objects.nonNull(_cellIterator)) {
                    _next = _cellIterator.hasNext() ? SheetToken.CELL_VALUE : SheetToken.ROW_END;
                } else {
                    _next = _mainRowIterator.hasNext() ? SheetToken.ROW_START : SheetToken.SHEET_DATA_END;
                }
            }
            case SHEET_DATA_END -> _next = null;
        }
        if (log.isTraceEnabled()) {
            if (token == SheetToken.CELL_VALUE) {
                log.trace("{} {} {}", token, getReference(), getCellValue());
            } else {
                log.trace("{}", token);
            }
        }
        return token;
    }

    private String _formattedString(final double value, final CellStyle style) {
        if (style != null && style.getDataFormatString() != null) {
            return _formatter.formatRawCellContents(value, style.getDataFormat(), style.getDataFormatString());
        }
        return null;
    }
}
