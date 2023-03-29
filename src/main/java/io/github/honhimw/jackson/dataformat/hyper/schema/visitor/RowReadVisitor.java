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

import io.github.honhimw.jackson.dataformat.hyper.deser.CellValue;
import io.github.honhimw.jackson.dataformat.hyper.schema.Column;
import org.apache.poi.ss.usermodel.Cell;

/**
 * @author hon_him
 * @since 2023-03-20
 */

public abstract class RowReadVisitor {

    protected RowReadVisitor _readVisitor;

    public RowReadVisitor() {
    }

    public RowReadVisitor(final RowReadVisitor _readVisitor) {
        this._readVisitor = _readVisitor;
    }

    /**
     * read value
     *
     * @param cell   to be read
     * @param column column-level schema
     * @return value holder
     */
    public CellValue visitCell(Cell cell, Column column) {
        if (this._readVisitor != null) {
            return this._readVisitor.visitCell(cell, column);
        }
        return null;
    }

}
