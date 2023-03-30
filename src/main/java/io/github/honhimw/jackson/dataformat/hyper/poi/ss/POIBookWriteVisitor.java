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
import io.github.honhimw.jackson.dataformat.hyper.schema.Table;
import io.github.honhimw.jackson.dataformat.hyper.schema.visitor.BookWriteVisitor;
import io.github.honhimw.jackson.dataformat.hyper.schema.visitor.SheetWriteVisitor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author hon_him
 * @since 2023-03-20
 */

final class POIBookWriteVisitor extends BookWriteVisitor {

    @Override
    public void init(final BookWriteVisitor writeVisitor) {
    }

    @Override
    public void visitBook(final Workbook workbook, final HyperSchema schema) {
    }

    @Override
    public SheetWriteVisitor visitSheet(final Sheet sheet, final Table table) {
        return new POISheetWriteVisitor(sheet, table);
    }

    @Override
    public void visitEnd() {
    }
}
