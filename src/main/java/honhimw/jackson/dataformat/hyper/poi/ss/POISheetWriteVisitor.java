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

import honhimw.jackson.dataformat.hyper.schema.Table;
import honhimw.jackson.dataformat.hyper.schema.visitor.RowWriteVisitor;
import honhimw.jackson.dataformat.hyper.schema.visitor.SheetWriteVisitor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * @author hon_him
 * @since 2023-03-20
 */

final class POISheetWriteVisitor extends SheetWriteVisitor {

    private final Sheet _sheet;
    private final Table _table;

    POISheetWriteVisitor(final Sheet sheet, final Table table) {
        this._sheet = sheet;
        this._table = table;
    }

    @Override
    public RowWriteVisitor visitHeaders(final Row row) {
        return new POIRowWriteVisitor(row);
    }

    @Override
    public RowWriteVisitor visitRow(final Row row, final Object value) {
        return super.visitRow(row, value);
    }
}
