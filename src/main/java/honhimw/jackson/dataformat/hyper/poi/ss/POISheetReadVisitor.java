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
