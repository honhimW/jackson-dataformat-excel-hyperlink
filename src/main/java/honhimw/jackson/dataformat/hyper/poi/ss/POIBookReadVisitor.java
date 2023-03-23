package honhimw.jackson.dataformat.hyper.poi.ss;

import honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import honhimw.jackson.dataformat.hyper.schema.Table;
import honhimw.jackson.dataformat.hyper.schema.visitor.BookReadVisitor;
import honhimw.jackson.dataformat.hyper.schema.visitor.SheetReadVisitor;
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
