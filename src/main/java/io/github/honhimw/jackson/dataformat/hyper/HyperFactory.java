/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.honhimw.jackson.dataformat.hyper;

import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.io.IOContext;
import io.github.honhimw.jackson.dataformat.hyper.deser.BookInput;
import io.github.honhimw.jackson.dataformat.hyper.deser.BookParser;
import io.github.honhimw.jackson.dataformat.hyper.deser.BookReader;
import io.github.honhimw.jackson.dataformat.hyper.poi.ooxml.PackageUtil;
import io.github.honhimw.jackson.dataformat.hyper.poi.ooxml.SSMLBookReader;
import io.github.honhimw.jackson.dataformat.hyper.poi.ooxml.SSMLWorkbook;
import io.github.honhimw.jackson.dataformat.hyper.poi.ss.POIBookReader;
import io.github.honhimw.jackson.dataformat.hyper.poi.ss.POIBookWriter;
import io.github.honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import io.github.honhimw.jackson.dataformat.hyper.ser.BookOutput;
import io.github.honhimw.jackson.dataformat.hyper.ser.BookWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

@SuppressWarnings("java:S2177")
public final class HyperFactory extends JsonFactory {

    public static final String FORMAT_NAME = "hyper";
    public static final int DEFAULT_SHEET_PARSER_FEATURE_FLAGS = BookParser.Feature.collectDefaults();

    private final transient WorkbookProvider _workbookProvider;
    private int _sheetParserFeatures;

    public HyperFactory() {
        this(SXSSFWorkbook::new, DEFAULT_SHEET_PARSER_FEATURE_FLAGS);
    }

    public HyperFactory(final WorkbookProvider workbookProvider, final int sheetParserFeatures) {
        _workbookProvider = workbookProvider;
        _sheetParserFeatures = sheetParserFeatures;
    }

    public HyperFactory(final HyperFactory base) {
        _workbookProvider = base._workbookProvider;
        _sheetParserFeatures = base._sheetParserFeatures;
    }

    @Override
    public HyperFactory copy() {
        _checkInvalidCopy(HyperFactory.class);
        return new HyperFactory(this);
    }

    /*
    /**********************************************************
    /* Format detection functionality
    /**********************************************************
     */

    @Override
    public boolean canUseSchema(final FormatSchema schema) {
        return schema instanceof HyperSchema;
    }

    @Override
    public String getFormatName() {
        return FORMAT_NAME;
    }

    /*
    /**********************************************************
    /* Versioned
    /**********************************************************
     */

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    /*
    /**********************************************************
    /* Configuration, sheet parser configuration
    /**********************************************************
     */

    public HyperFactory configure(final BookParser.Feature f, final boolean state) {
        return state ? enable(f) : disable(f);
    }

    public HyperFactory enable(final BookParser.Feature f) {
        _sheetParserFeatures |= f.getMask();
        return this;
    }

    public HyperFactory disable(final BookParser.Feature f) {
        _sheetParserFeatures &= ~f.getMask();
        return this;
    }

    /*
    /**********************************************************
    /* Parser factories
    /**********************************************************
     */

    public BookParser createParser(final Workbook src) {
        final IOContext ctxt = _createContext(_createContentReference(src), false);
        return _createParser(new POIBookReader(src), ctxt);
    }

    @Override
    public BookParser createParser(final File src) throws IOException {
        final IOContext ctxt = _createContext(_createContentReference(src), true);
        final BookReader reader = _createFileSheetReader(src);
        return _createParser(reader, ctxt);
    }

    @Override
    public BookParser createParser(final InputStream src) throws IOException {
        final IOContext ctxt = _createContext(_createContentReference(src), true);
        final BookReader reader = _createInputStreamSheetReader(src);
        return _createParser(reader, ctxt);
    }

    /*
    /**********************************************************
    /* Generator factories
    /**********************************************************
     */

    public HyperGenerator createGenerator(final BookOutput<?> out) throws IOException {
        final BookOutput<OutputStream> output = _rawAsOutputStream(out);
        final boolean resourceManaged = out != output;
        final IOContext ctxt = _createContext(_createContentReference(output), resourceManaged);
        return _createGenerator(_createSheetWriter(output), ctxt);
    }

    @Override
    public HyperGenerator createGenerator(final File out, final JsonEncoding enc) throws IOException {
        return createGenerator(BookOutput.target(out));
    }

    @Override
    public HyperGenerator createGenerator(final OutputStream out, final JsonEncoding enc) throws IOException {
        return createGenerator(BookOutput.target(out));
    }

    @Override
    public HyperGenerator createGenerator(final OutputStream out) throws IOException {
        return createGenerator(BookOutput.target(out));
    }

    /*
    /**********************************************************
    /* Factory methods used by factory for creating parser instances,
    /**********************************************************
     */

    private BookParser _createParser(final BookReader reader, final IOContext ctxt) {
        return new BookParser(ctxt, _parserFeatures, _objectCodec, _sheetParserFeatures, reader);
    }

    private BookReader _createFileSheetReader(final File src) throws IOException {
        return _createPOISheetReader(WorkbookFactory.create(src));
    }

    private BookReader _createInputStreamSheetReader(final InputStream src) throws IOException {
        return _createPOISheetReader(WorkbookFactory.create(src));
    }

    private SSMLBookReader _createSSMLSheetReader(final SSMLWorkbook workbook, final BookInput<?> src) {
        final PackagePart worksheetPart =
            src.isNamed() ? workbook.getWorksheetPart(src.getName()) : workbook.getWorksheetPartAt(src.getIndex());
        if (worksheetPart == null) {
            throw new IllegalArgumentException("No sheet for " + src);
        }
        return new SSMLBookReader(worksheetPart, workbook);
    }

    private POIBookReader _createPOISheetReader(final Workbook workbook) {
        return new POIBookReader(workbook);
    }

    @SuppressWarnings("unchecked")
    private BookInput<?> _preferRawAsFile(final BookInput<?> src) throws IOException {
        if (src.isFile()) {
            return src;
        }
        final InputStream raw = ((BookInput<InputStream>) src).getRaw();
        if (!PackageUtil.isOOXML(raw)) {
            return src;
        }
        final File file = TempFile.createTempFile("sheet-input", ".xlsx");
        Files.copy(raw, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        if (isEnabled(StreamReadFeature.AUTO_CLOSE_SOURCE)) {
            raw.close();
        }
        return src.isNamed() ? BookInput.source(file, src.getName()) : BookInput.source(file, src.getIndex());
    }

    /*
    /**********************************************************
    /* Factory methods used by factory for creating generator instances,
    /**********************************************************
     */

    private HyperGenerator _createGenerator(final BookWriter writer, final IOContext ctxt) {
        return new HyperGenerator(ctxt, _generatorFeatures, _objectCodec, writer);
    }

    @SuppressWarnings("resource")
    private BookWriter _createSheetWriter(final BookOutput<?> out) throws IOException {
        final Workbook workbook = _workbookProvider.create();
//        final Sheet sheet;
//        if (out.isNamed()) {
//            sheet = workbook.createSheet(out.getName());
//        } else {
//            sheet = workbook.createSheet();
//        }
        return new POIBookWriter(workbook);
    }

    @SuppressWarnings("unchecked")
    private BookOutput<OutputStream> _rawAsOutputStream(final BookOutput<?> out) throws IOException {
        if (!out.isFile()) {
            return (BookOutput<OutputStream>) out;
        }
        final OutputStream raw = Files.newOutputStream(((File) out.getRaw()).toPath());
        return out.isNamed() ? BookOutput.target(raw, out.getName()) : BookOutput.target(raw);
    }
}
