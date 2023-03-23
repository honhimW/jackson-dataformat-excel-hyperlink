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
import honhimw.jackson.dataformat.hyper.poi.RetainedSheets;
import honhimw.jackson.dataformat.hyper.schema.Column;
import honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import honhimw.jackson.dataformat.hyper.schema.Table;
import honhimw.jackson.dataformat.hyper.schema.visitor.BookReadVisitor;
import honhimw.jackson.dataformat.hyper.schema.visitor.RowReadVisitor;
import honhimw.jackson.dataformat.hyper.schema.visitor.SheetReadVisitor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

@Slf4j
public final class POIBookReader implements BookReader {

    public final static Pattern HYPER_LINK_PATTERN = Pattern.compile("#(?<sheet>.*)!(?<first>\\d+):(?<last>\\d+)");

    private final Workbook _workbook;
    private final Map<String, Sheet> _sheetMap = new HashMap<>();
    private HyperSchema _schema;
    private Sheet _mainSheet;
    private Iterator<Row> _mainRowIterator;
    private Iterator<Cell> _cellIterator;
    private BookReadVisitor _bookReadVisitor;
    private SheetReadVisitor _sheetReadVisitor;
    private RowReadVisitor _rowReadVisitor;
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
    public void setSchema(HyperSchema schema) {
        this._schema = schema;
        final Table mainTable = _schema.getTables().get(0);
        _mainSheet = _workbook.getSheet(mainTable.getName());
        _workbook.sheetIterator().forEachRemaining(rows -> _sheetMap.put(rows.getSheetName(), rows));
        _mainRowIterator = _mainSheet.rowIterator();
        BookReadVisitor bookReadVisitor = _schema.getBookReadVisitor();
        if (bookReadVisitor != null) {
            accept(bookReadVisitor);
        }
        _bookReadVisitor.visitBook(_workbook, _schema);
    }

    private void accept(BookReadVisitor bookReadVisitor) {
        _bookReadVisitor = new POIBookReadVisitor();
        bookReadVisitor.init(_bookReadVisitor);
        this._bookReadVisitor = bookReadVisitor;
    }

    @Override
    public CellAddress getReference() {
        return _cell == null ? null : _cell.getAddress();
    }

    @Override
    public CellValue getCellValue() {
        if (_cell == null) {
            return null;
        }
        Column column = null;
        if (!RetainedSheets.isRetain(_cell.getSheet().getSheetName())) {
            column = _schema.getColumn(_cell.getSheet().getSheetName(), _cell.getAddress());
        }
        return _rowReadVisitor.visitCell(_cell, column);
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
        _bookReadVisitor.visitEnd();
        _closed = true;
    }

    @Override
    public boolean hasNext() {
        return _next != null;
    }

    @Override
    public SheetToken next() {
        if (_next == null) {
            throw new NoSuchElementException();
        }
        final SheetToken token = _next;
        switch (token) {
            case SHEET_DATA_START -> {
                _sheetReadVisitor = _bookReadVisitor.visitSheet(_mainSheet);
                _next = _mainRowIterator.hasNext() ? SheetToken.ROW_START : SheetToken.SHEET_DATA_END;
            }
            case ROW_START -> {
                final Row row = _mainRowIterator.next();
                _rowIndex = row.getRowNum();
                _cellIterator = row.cellIterator();
                _rowReadVisitor = _sheetReadVisitor.visitRow(row);
                _next = _cellIterator.hasNext() ? SheetToken.CELL_VALUE : SheetToken.ROW_END;
            }
            case CELL_VALUE -> {
                _cell = _cellIterator.next();
                _columnIndex = _cell.getColumnIndex();
                _next = _cellIterator.hasNext() ? SheetToken.CELL_VALUE : SheetToken.ROW_END;
                Hyperlink hyperlink = _cell.getHyperlink();
                if (Objects.nonNull(hyperlink)) {
                    Matcher matcher = HYPER_LINK_PATTERN.matcher(hyperlink.getAddress());
                    if (matcher.find()) {
                        String label = matcher.group("sheet");
                        String first = matcher.group("first");
                        int firstRow = Integer.parseInt(first);
                        Sheet sheet = _sheetMap.get(label);
                        _sheetReadVisitor = _bookReadVisitor.visitSheet(sheet);
                        Row linkedRow = sheet.getRow(firstRow - 1);
                        _rowReadVisitor = _sheetReadVisitor.visitRow(linkedRow);
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
            case SHEET_DATA_END -> {
                _next = null;
                _bookReadVisitor.visitEnd();
            }
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
}
