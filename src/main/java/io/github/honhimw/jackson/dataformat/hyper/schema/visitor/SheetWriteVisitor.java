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

import org.apache.poi.ss.usermodel.Row;

/**
 * Visitor for sheet writing
 *
 * @author hon_him
 * @since 2023-03-20
 */

public abstract class SheetWriteVisitor {

    protected SheetWriteVisitor _writeVisitor;

    public SheetWriteVisitor() {
        this(null);
    }

    public SheetWriteVisitor(final SheetWriteVisitor _writeVisitor) {
        this._writeVisitor = _writeVisitor;
    }

    /**
     * called once-per-sheet
     *
     * @param row where the headers to be wrote
     */
    public RowWriteVisitor visitHeaders(Row row) {
        if (this._writeVisitor != null) {
            return _writeVisitor.visitHeaders(row);
        }
        return null;
    }

    /**
     * beginning of object/array
     *
     * @param row   the row to be wrote
     * @param value object/array
     */
    public RowWriteVisitor visitRow(Row row, Object value) {
        if (this._writeVisitor != null) {
            return _writeVisitor.visitRow(row, value);
        }
        return null;
    }
}
