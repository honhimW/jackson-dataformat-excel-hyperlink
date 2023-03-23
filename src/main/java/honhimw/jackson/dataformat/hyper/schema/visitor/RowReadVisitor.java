package honhimw.jackson.dataformat.hyper.schema.visitor;

import honhimw.jackson.dataformat.hyper.deser.CellValue;
import honhimw.jackson.dataformat.hyper.schema.Column;
import org.apache.poi.ss.usermodel.Cell;

/**
 * @author hon_him
 * @since 2023-03-20
 */

public abstract class RowReadVisitor {

    protected RowReadVisitor _readVisitor;

    public RowReadVisitor() {
    }

    public RowReadVisitor(final RowReadVisitor _readVisitor) {
        this._readVisitor = _readVisitor;
    }

    /**
     * read value
     * @param cell   to be read
     * @param column column-level schema
     * @return value holder
     */
    public CellValue visitCell(Cell cell, Column column) {
        if (this._readVisitor != null) {
            return this._readVisitor.visitCell(cell, column);
        }
        return null;
    }

}
