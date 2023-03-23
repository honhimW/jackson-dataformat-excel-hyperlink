package honhimw.jackson.dataformat.hyper.poi.ss;

import honhimw.jackson.dataformat.hyper.schema.Column;
import honhimw.jackson.dataformat.hyper.schema.visitor.RowWriteVisitor;
import java.util.function.BiConsumer;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * @author hon_him
 * @since 2023-03-22
 */

final class POIRowWriteVisitor extends RowWriteVisitor {

    private final Row _row;

    POIRowWriteVisitor(final Row _row) {
        this._row = _row;
    }

    @Override
    public void visitHeader(final Cell cell, final Column column) {
        cell.setCellValue(column.getName());
    }

    @Override
    public  <T> void visitCell(final Cell cell, final Column column, final T value,
        final BiConsumer<Cell, T> consumer) {
        consumer.accept(cell, value);
    }
}
