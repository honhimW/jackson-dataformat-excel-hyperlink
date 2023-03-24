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

import honhimw.jackson.dataformat.hyper.schema.visitor.RowReadVisitor;
import honhimw.jackson.dataformat.hyper.schema.visitor.SheetReadVisitor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * @author hon_him
 * @since 2023-03-23
 */

final class POISheetReadVisitor extends SheetReadVisitor {

    private final Sheet _sheet;

    POISheetReadVisitor(final Sheet sheet) {
        this._sheet = sheet;
    }

    @Override
    public RowReadVisitor visitRow(final Row row) {
        return new POIRowReadVisitor(row);
    }
}
