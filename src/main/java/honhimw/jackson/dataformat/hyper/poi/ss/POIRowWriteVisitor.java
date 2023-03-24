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
import honhimw.jackson.dataformat.hyper.schema.visitor.RowWriteVisitor;
import java.util.function.BiConsumer;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * @author hon_him
 * @since 2023-03-22
 */

final class POIRowWriteVisitor extends RowWriteVisitor {

    private final Row _row;

    POIRowWriteVisitor(final Row _row) {
        this._row = _row;
    }

    @Override
    public void visitHeader(final Cell cell, final Column column) {
        cell.setCellValue(column.getName());
    }

    @Override
    public <T> void visitCell(final Cell cell, final Column column, final T value,
        final BiConsumer<Cell, T> consumer) {
        consumer.accept(cell, value);
    }
}
