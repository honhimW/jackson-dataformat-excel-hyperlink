package honhimw.jackson.dataformat.hyper.poi.ss;

import honhimw.jackson.dataformat.hyper.schema.Table;
import honhimw.jackson.dataformat.hyper.schema.visitor.RowWriteVisitor;
import honhimw.jackson.dataformat.hyper.schema.visitor.SheetWriteVisitor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;

/**
 * @author hon_him
 * @since 2023-03-20
 */

final class POISheetWriteVisitor extends SheetWriteVisitor {

    private final Sheet _sheet;
    private final Table _table;

    POISheetWriteVisitor(final Sheet sheet, final Table table) {
        this._sheet = sheet;
        this._table = table;
    }

    @Override
    public RowWriteVisitor visitHeaders(final Row row) {
        return new POIRowWriteVisitor(row);
    }

    @Override
    public RowWriteVisitor visitRow(final Row row, final Object value) {
        return super.visitRow(row, value);
    }
}
