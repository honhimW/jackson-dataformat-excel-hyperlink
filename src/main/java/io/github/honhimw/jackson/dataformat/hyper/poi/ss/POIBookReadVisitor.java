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

import io.github.honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import io.github.honhimw.jackson.dataformat.hyper.schema.visitor.BookReadVisitor;
import io.github.honhimw.jackson.dataformat.hyper.schema.visitor.SheetReadVisitor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author hon_him
 * @since 2023-03-23
 */

final class POIBookReadVisitor extends BookReadVisitor {

    @Override
    public void init(final BookReadVisitor readVisitor) {
    }

    @Override
    public void visitBook(final Workbook workbook, final HyperSchema schema) {
    }

    @Override
    public SheetReadVisitor visitSheet(final Sheet sheet) {
        return new POISheetReadVisitor(sheet);
    }

    @Override
    public void visitEnd() {
    }
}
