package honhimw.jackson.dataformat.hyper.schema.visitor;

import honhimw.jackson.dataformat.hyper.deser.CellValue;
import honhimw.jackson.dataformat.hyper.schema.Column;
import honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import honhimw.jackson.dataformat.hyper.schema.Table;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author hon_him
 * @since 2023-03-16
 */

public abstract class BookReadVisitor {

    protected BookReadVisitor _readVisitor;

    public BookReadVisitor() {
        this(null);
    }

    public BookReadVisitor(BookReadVisitor writeVisitor) {
        this._readVisitor = writeVisitor;
    }

    /**
     * setup principal
     *
     * @param readVisitor principal
     */
    public void init(BookReadVisitor readVisitor) {
        this._readVisitor = readVisitor;
    }

    /**
     * start of the book reading
     *
     * @param workbook workbook to be read
     * @param schema   schema for workbook
     */
    public void visitBook(Workbook workbook, HyperSchema schema) {
        if (this._readVisitor != null) {
            this._readVisitor.visitBook(workbook, schema);
        }
    }

    /**
     * context changed
     *
     * @param sheet to be entered
     */
    public SheetReadVisitor visitSheet(Sheet sheet) {
        if (this._readVisitor != null) {
            return this._readVisitor.visitSheet(sheet);
        }
        return null;
    }

    public void visitEnd() {
        if (this._readVisitor != null) {
            _readVisitor.visitEnd();
        }
    }

}
