package honhimw.jackson.dataformat.hyper.schema.visitor;

import honhimw.jackson.dataformat.hyper.schema.Column;
import java.util.function.BiConsumer;
import org.apache.poi.ss.usermodel.Cell;

/**
 * Visitor for row writing
 *
 * @author hon_him
 * @since 2023-03-20
 */

public abstract class RowWriteVisitor {

    protected RowWriteVisitor _writeVisitor;

    public RowWriteVisitor() {
        this(null);
    }

    public RowWriteVisitor(final RowWriteVisitor _writeVisitor) {
        this._writeVisitor = _writeVisitor;
    }

    /**
     * called once-per-column, cell value should always be string type
     *
     * @param cell   column header
     * @param column column-level schema
     */
    public void visitHeader(Cell cell, Column column) {
        if (this._writeVisitor != null) {
            _writeVisitor.visitHeader(cell, column);
        }
    }

    /**
     * write value
     *
     * @param cell     to be wrote
     * @param column   column-level schema for current value
     * @param value    value for current cell
     * @param consumer basic write method
     */
    public <T> void visitCell(Cell cell, Column column, final T value, final BiConsumer<Cell, T> consumer) {
        if (this._writeVisitor != null) {
            _writeVisitor.visitCell(cell, column, value, consumer);
        }
    }
}
