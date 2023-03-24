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

package honhimw.jackson.dataformat.hyper.schema.visitor;

import honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author hon_him
 * @since 2023-03-16
 */

public abstract class BookReadVisitor {

    protected BookReadVisitor _readVisitor;

    public BookReadVisitor() {
        this(null);
    }

    public BookReadVisitor(BookReadVisitor writeVisitor) {
        this._readVisitor = writeVisitor;
    }

    /**
     * setup principal
     *
     * @param readVisitor principal
     */
    public void init(BookReadVisitor readVisitor) {
        this._readVisitor = readVisitor;
    }

    /**
     * start of the book reading
     *
     * @param workbook workbook to be read
     * @param schema   schema for workbook
     */
    public void visitBook(Workbook workbook, HyperSchema schema) {
        if (this._readVisitor != null) {
            this._readVisitor.visitBook(workbook, schema);
        }
    }

    /**
     * context changed
     *
     * @param sheet to be entered
     */
    public SheetReadVisitor visitSheet(Sheet sheet) {
        if (this._readVisitor != null) {
            return this._readVisitor.visitSheet(sheet);
        }
        return null;
    }

    public void visitEnd() {
        if (this._readVisitor != null) {
            _readVisitor.visitEnd();
        }
    }

}
