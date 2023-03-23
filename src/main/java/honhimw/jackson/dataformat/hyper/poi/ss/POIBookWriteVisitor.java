package honhimw.jackson.dataformat.hyper.poi.ss;

import honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import honhimw.jackson.dataformat.hyper.schema.Table;
import honhimw.jackson.dataformat.hyper.schema.visitor.BookWriteVisitor;
import honhimw.jackson.dataformat.hyper.schema.visitor.SheetWriteVisitor;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;

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
