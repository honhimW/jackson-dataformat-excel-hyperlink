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

import io.github.honhimw.jackson.dataformat.hyper.deser.CellValue;
import io.github.honhimw.jackson.dataformat.hyper.schema.Column;
import io.github.honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import io.github.honhimw.jackson.dataformat.hyper.schema.Table;
import io.github.honhimw.jackson.dataformat.hyper.schema.visitor.BookReadVisitor;
import io.github.honhimw.jackson.dataformat.hyper.schema.visitor.SheetReadVisitor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author hon_him
 * @since 2023-03-23
 */

final class POIBookReadVisitor extends BookReadVisitor {

    private final boolean _reorder;

    public POIBookReadVisitor(boolean reorder) {
        this._reorder = reorder;
    }

    @Override
    public void init(final BookReadVisitor readVisitor) {
    }

    @Override
    public void visitBook(final Workbook workbook, final HyperSchema schema) {
        if (_reorder) {
            List<Table> tables = schema.getTables();
            for (final Table table : tables) {
                Sheet sheet = workbook.getSheet(table.getName());
                if (Objects.nonNull(sheet)) {
                    Row headers = sheet.getRow(schema.getOriginRow());
                    Objects.requireNonNull(headers, "Header row should not be null when [REORDER] feature is enabled.");
                    Iterator<Cell> cellIterator = headers.cellIterator();
                    Map<String, Integer> orderMap = new HashMap<>();
                    AtomicInteger index = new AtomicInteger(schema.getOriginColumn());
                    while (cellIterator.hasNext()) {
                        Cell header = cellIterator.next();
                        CellValue cellValue = new CellValue(header);
                        String headerStr = String.valueOf(cellValue.getValue());
                        orderMap.compute(headerStr, (name, _index) -> Objects.nonNull(_index) ? _index : index.get());
                        index.incrementAndGet();
                    }
                    List<Column> columns = table.getColumns();
                    List<Column> tmp = new ArrayList<>(columns);
                    Map<String, Column> nameMap = tmp.stream().collect(Collectors.toMap(Column::getName, column -> column));
                    columns.clear();
                    Column[] cs = new Column[index.get()];
                    orderMap.forEach((name, _index) -> {
                        cs[_index] = nameMap.get(name);
                    });
                    columns.addAll(Arrays.asList(cs));
                }
            }
        }
    }

    @Override
    public SheetReadVisitor visitSheet(final Sheet sheet) {
        return new POISheetReadVisitor(sheet);
    }

    @Override
    public void visitEnd() {
    }
}
