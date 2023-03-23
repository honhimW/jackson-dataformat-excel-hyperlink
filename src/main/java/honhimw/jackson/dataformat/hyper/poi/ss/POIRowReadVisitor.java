package honhimw.jackson.dataformat.hyper.poi.ss;

import honhimw.jackson.dataformat.hyper.deser.CellValue;
import honhimw.jackson.dataformat.hyper.schema.Column;
import honhimw.jackson.dataformat.hyper.schema.visitor.RowReadVisitor;
import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

/**
 * @author hon_him
 * @since 2023-03-23
 */

final class POIRowReadVisitor extends RowReadVisitor {

    private static final DataFormatter _formatter = new DataFormatter();

    private final Row _row;

    POIRowReadVisitor(final Row row) {
        this._row = row;
    }

    @Override
    public CellValue visitCell(final Cell cell, final Column column) {
        final CellType type = CellFormat.ultimateType(cell);
        switch (type) {
            case NUMERIC:
                final double value = cell.getNumericCellValue();
                return new CellValue(value, _formattedString(value, cell.getCellStyle()));
            case STRING:
                return new CellValue(cell.getStringCellValue());
            case BOOLEAN:
                return CellValue.valueOf(cell.getBooleanCellValue());
            case ERROR:
                return CellValue.getError(cell.getErrorCellValue());
            case BLANK:
                return null;
            case _NONE:
            case FORMULA:
        }
        throw new IllegalStateException("Unexpected cell value type: " + type);
    }

    private String _formattedString(final double value, final CellStyle style) {
        if (style != null && style.getDataFormatString() != null) {
            return _formatter.formatRawCellContents(value, style.getDataFormat(), style.getDataFormatString());
        }
        return null;
    }
}
