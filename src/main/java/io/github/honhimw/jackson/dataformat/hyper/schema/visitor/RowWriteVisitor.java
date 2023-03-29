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

package io.github.honhimw.jackson.dataformat.hyper.schema.visitor;

import io.github.honhimw.jackson.dataformat.hyper.schema.Column;
import java.util.function.BiConsumer;
import org.apache.poi.ss.usermodel.Cell;

/**
 * Visitor for row writing
 *
 * @author hon_him
 * @since 2023-03-20
 */

public abstract class RowWriteVisitor {

    protected RowWriteVisitor _writeVisitor;

    public RowWriteVisitor() {
        this(null);
    }

    public RowWriteVisitor(final RowWriteVisitor _writeVisitor) {
        this._writeVisitor = _writeVisitor;
    }

    /**
     * called once-per-column, cell value should always be string type
     *
     * @param cell   column header
     * @param column column-level schema
     */
    public void visitHeader(Cell cell, Column column) {
        if (this._writeVisitor != null) {
            _writeVisitor.visitHeader(cell, column);
        }
    }

    /**
     * write value
     *
     * @param cell     to be wrote
     * @param column   column-level schema for current value
     * @param value    value for current cell
     * @param consumer basic write method
     */
    public <T> void visitCell(Cell cell, Column column, final T value, final BiConsumer<Cell, T> consumer) {
        if (this._writeVisitor != null) {
            _writeVisitor.visitCell(cell, column, value, consumer);
        }
    }
}
