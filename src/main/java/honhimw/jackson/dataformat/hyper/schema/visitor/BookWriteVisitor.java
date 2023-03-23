package honhimw.jackson.dataformat.hyper.schema.visitor;

import honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import honhimw.jackson.dataformat.hyper.schema.Table;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Visitor for book writing
 *
 * @author hon_him
 * @since 2023-03-16
 */

public abstract class BookWriteVisitor {

    protected BookWriteVisitor _writeVisitor;

    public BookWriteVisitor() {
        this(null);
    }

    public BookWriteVisitor(BookWriteVisitor writeVisitor) {
        this._writeVisitor = writeVisitor;
    }

    /**
     * setup principal
     *
     * @param writeVisitor principal
     */
    public void init(BookWriteVisitor writeVisitor) {
        if (this._writeVisitor != null) {
            writeVisitor.init(_writeVisitor);
        }
        this._writeVisitor = writeVisitor;
    }

    /**
     * start of the book writing
     *
     * @param workbook workbook to be wrote
     * @param schema   schema for workbook
     */
    public void visitBook(Workbook workbook, HyperSchema schema) {
        if (this._writeVisitor != null) {
            this._writeVisitor.visitBook(workbook, schema);
        }
    }

    /**
     * context changed
     *
     * @param sheet to be entered
     * @param table sheet-level schema
     */
    public SheetWriteVisitor visitSheet(Sheet sheet, Table table) {
        if (this._writeVisitor != null) {
            return this._writeVisitor.visitSheet(sheet, table);
        }
        return null;
    }

    /**
     * end of the book writing
     */
    public void visitEnd() {
        if (this._writeVisitor != null) {
            _writeVisitor.visitEnd();
        }
    }

}
