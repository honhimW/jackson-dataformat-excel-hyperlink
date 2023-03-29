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

import org.apache.poi.ss.usermodel.Row;

/**
 * @author hon_him
 * @since 2023-03-20
 */

public abstract class SheetReadVisitor {

    protected SheetReadVisitor _readVisitor;

    public SheetReadVisitor() {
    }

    public SheetReadVisitor(final SheetReadVisitor readVisitor) {
        this._readVisitor = readVisitor;
    }

    /**
     * called once-per-row
     *
     * @param row the row to be read which contain values
     */
    public RowReadVisitor visitRow(Row row) {
        if (this._readVisitor != null) {
            return this._readVisitor.visitRow(row);
        }
        return null;
    }

}
