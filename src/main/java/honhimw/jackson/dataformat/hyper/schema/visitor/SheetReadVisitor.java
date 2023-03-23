package honhimw.jackson.dataformat.hyper.schema.visitor;

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
     * @param row the row to be read which contain values
     */
    public RowReadVisitor visitRow(Row row) {
        if (this._readVisitor != null) {
            return this._readVisitor.visitRow(row);
        }
        return null;
    }

}
