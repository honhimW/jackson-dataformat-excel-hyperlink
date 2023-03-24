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

package honhimw.jackson.dataformat.hyper;

import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import honhimw.jackson.dataformat.hyper.deser.BookParser;
import honhimw.jackson.dataformat.hyper.schema.HyperSchema;
import honhimw.jackson.dataformat.hyper.schema.generator.ColumnNameResolver;
import honhimw.jackson.dataformat.hyper.schema.generator.TableNameResolver;
import honhimw.jackson.dataformat.hyper.schema.visitor.BookReadVisitor;
import honhimw.jackson.dataformat.hyper.schema.visitor.BookWriteVisitor;
import honhimw.jackson.dataformat.hyper.ser.BookOutput;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Function;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellAddress;

@SuppressWarnings("java:S2177")
public final class HyperMapper extends ObjectMapper {

    private transient SchemaGenerator _schemaGenerator;

    public HyperMapper() {
        this(new HyperFactory());
    }

    public HyperMapper(final HyperFactory f) {
        super(f);
        registerModule(new HyperModule());
        _schemaGenerator = new SchemaGenerator();
    }

    private HyperMapper(final HyperMapper src) {
        super(src);
        _schemaGenerator = src._schemaGenerator;
    }

    public static Builder builder() {
        return new Builder(new HyperMapper());
    }

    public static Builder builder(final HyperFactory streamFactory) {
        return new Builder(new HyperMapper(streamFactory));
    }

    public Builder rebuild() {
        return new Builder(copy());
    }

    @Override
    public HyperMapper copy() {
        _checkInvalidCopy(HyperMapper.class);
        return new HyperMapper(this);
    }

    @Override
    public HyperMapper copyWith(final JsonFactory factory) {
        return (HyperMapper) super.copyWith(factory);
    }

    /*
    /**********************************************************
    /* SpreadsheetReader/SpreadsheetWriter implementations
    /**********************************************************
     */

    @Override
    protected HyperReader _newReader(DeserializationConfig config) {
        return new HyperReader(this, config);
    }

    @Override
    protected HyperReader _newReader(final DeserializationConfig config, final JavaType valueType,
        final Object valueToUpdate, final FormatSchema schema,
        final InjectableValues injectableValues) {
        return new HyperReader(this, config, valueType, valueToUpdate, schema, injectableValues);
    }

    @Override
    protected HyperWriter _newWriter(final SerializationConfig config) {
        return new HyperWriter(this, config);
    }

    @Override
    protected HyperWriter _newWriter(final SerializationConfig config, final FormatSchema schema) {
        return new HyperWriter(this, config, schema);
    }

    @Override
    protected HyperWriter _newWriter(final SerializationConfig config, final JavaType rootType,
        final PrettyPrinter pp) {
        return new HyperWriter(this, config, rootType, pp);
    }

    /*
    /**********************************************************
    /* Versioned impl
    /**********************************************************
     */

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    /*
    /**********************************************************
    /* Module registration, discovery
    /**********************************************************
     */

    @Override
    public HyperMapper registerModule(final com.fasterxml.jackson.databind.Module module) {
        return (HyperMapper) super.registerModule(module);
    }

    @Override
    public HyperMapper registerModules(final com.fasterxml.jackson.databind.Module... modules) {
        return (HyperMapper) super.registerModules(modules);
    }

    @Override
    public HyperMapper registerModules(final Iterable<? extends com.fasterxml.jackson.databind.Module> modules) {
        return (HyperMapper) super.registerModules(modules);
    }

    /*
    /**********************************************************
    /* Factory methods for creating SheetGenerators
    /**********************************************************
     */

    public HyperGenerator createGenerator(final BookOutput<?> out) throws IOException {
        _assertNotNull("out", out);
        final HyperGenerator g = tokenStreamFactory().createGenerator(out);
        _serializationConfig.initialize(g);
        return g;
    }

    @Override
    public HyperGenerator createGenerator(final OutputStream out) throws IOException {
        return (HyperGenerator) super.createGenerator(out);
    }

    @Override
    public HyperGenerator createGenerator(final OutputStream out, final JsonEncoding enc) throws IOException {
        return (HyperGenerator) super.createGenerator(out, enc);
    }

    @Override
    public HyperGenerator createGenerator(final File outputFile, final JsonEncoding enc) throws IOException {
        return (HyperGenerator) super.createGenerator(outputFile, enc);
    }

    /*
    /**********************************************************
    /* Factory methods for creating SheetParsers
    /**********************************************************
     */

    @Override
    public BookParser createParser(final File src) throws IOException {
        return (BookParser) super.createParser(src);
    }

    @Override
    public BookParser createParser(final InputStream in) throws IOException {
        return (BookParser) super.createParser(in);
    }

    /*
    /**********************************************************
    /* Configuration, simple features: SheetParser.Feature
    /**********************************************************
     */

    public HyperMapper configure(final BookParser.Feature f, final boolean state) {
        tokenStreamFactory().configure(f, state);
        return this;
    }

    public HyperMapper enable(final BookParser.Feature... features) {
        for (BookParser.Feature f : features) {
            tokenStreamFactory().enable(f);
        }
        return this;
    }

    public HyperMapper disable(final BookParser.Feature... features) {
        for (BookParser.Feature f : features) {
            tokenStreamFactory().disable(f);
        }
        return this;
    }

    /*
    /**********************************************************
    /* Configuration, schema generation
    /**********************************************************
     */

    public SchemaGenerator getSchemaGenerator() {
        return _schemaGenerator;
    }

    public HyperMapper setSchemaGenerator(final SchemaGenerator generator) {
        _assertNotNull("generator", generator);
        _schemaGenerator = generator;
        return this;
    }

    public HyperMapper setOrigin(final int row, final int column) {
        return setOrigin(new CellAddress(row, column));
    }

    public HyperMapper setOrigin(final String address) {
        _assertNotNull("address", address);
        return setSchemaGenerator(_schemaGenerator.withOrigin(new CellAddress(address)));
    }

    public HyperMapper setOrigin(final CellAddress address) {
        _assertNotNull("address", address);
        return setSchemaGenerator(_schemaGenerator.withOrigin(address));
    }

    public HyperMapper setColumnNameResolver(final ColumnNameResolver resolver) {
        _assertNotNull("columnNameResolver", resolver);
        return setSchemaGenerator(_schemaGenerator.withColumnNameResolver(resolver));
    }

    public HyperMapper setTableNameResolver(final TableNameResolver resolver) {
        _assertNotNull("tableNameResolver", resolver);
        return setSchemaGenerator(_schemaGenerator.withTableNameResolver(resolver));
    }

    public HyperMapper acceptWriteVisitor(final Function<BookWriteVisitor, BookWriteVisitor> visitorBuilder) {
        _assertNotNull("BookWriteVisitor", visitorBuilder);
        return setSchemaGenerator(_schemaGenerator.withBookWriteVisitor(visitorBuilder));
    }

    public HyperMapper acceptReadVisitor(final Function<BookReadVisitor, BookReadVisitor> visitorBuilder) {
        _assertNotNull("BookReadVisitor", visitorBuilder);
        return setSchemaGenerator(_schemaGenerator.withBookReadVisitor(visitorBuilder));
    }

    /*
    /**********************************************************
    /* Configuration, other
    /**********************************************************
     */

    @Override
    public HyperFactory tokenStreamFactory() {
        return (HyperFactory) super.tokenStreamFactory();
    }

    /*
    /**********************************************************
    /* Public API, deserialization,
    /* convenience methods
    /**********************************************************
     */

    @Override
    public <T> T readValue(final File src, final Class<T> valueType) throws IOException {
        return sheetReaderFor(valueType).readValue(src);
    }

    @Override
    public <T> T readValue(final InputStream src, final Class<T> valueType) throws IOException {
        return sheetReaderFor(valueType).readValue(src);
    }

    public <T> List<T> readValues(final Workbook src, final Class<T> valueType) throws IOException {
        try (MappingIterator<T> iterator = sheetReaderFor(valueType).readValues(src)) {
            return iterator.readAll();
        }
    }

    public <T> List<T> readValues(final File src, final Class<T> valueType) throws IOException {
        try (MappingIterator<T> iterator = sheetReaderFor(valueType).readValues(src)) {
            return iterator.readAll();
        }
    }

    public <T> List<T> readValues(final InputStream src, final Class<T> valueType) throws IOException {
        try (MappingIterator<T> iterator = sheetReaderFor(valueType).readValues(src)) {
            return iterator.readAll();
        }
    }

    /*
    /**********************************************************
    /* Public API: serialization
    /* convenience methods
    /**********************************************************
     */

    public void writeValue(final BookOutput<?> out, final Object value) throws IOException {
        _verifyValueType(value);
        writeValue(out, value, value.getClass());
    }

    @Override
    public void writeValue(final File out, final Object value) throws IOException {
        _verifyValueType(value);
        writeValue(out, value, value.getClass());
    }

    @Override
    public void writeValue(final OutputStream out, final Object value) throws IOException {
        _verifyValueType(value);
        writeValue(out, value, value.getClass());
    }

    public void writeValue(final BookOutput<?> out, final Object value, final Class<?> valueType) throws IOException {
        sheetWriterFor(valueType).writeValue(out, value);
    }

    public void writeValue(final File out, final Object value, final Class<?> valueType) throws IOException {
        sheetWriterFor(valueType).writeValue(out, value);
    }

    public void writeValue(final OutputStream out, final Object value, final Class<?> valueType) throws IOException {
        sheetWriterFor(valueType).writeValue(out, value);
    }

    @Override
    public byte[] writeValueAsBytes(final Object value) throws JsonProcessingException {
        return writeValueAsBytes(value, value.getClass());
    }

    public byte[] writeValueAsBytes(final Object value, final Class<?> valueType) throws JsonProcessingException {
        return sheetWriterFor(valueType).writeValueAsBytes(value);
    }

    public Workbook writeValueAsBook(final Object value) throws IOException {
        return writeValueAsBook(value, value.getClass());
    }

    public Workbook writeValueAsBook(final Object value, final Class<?> valueType) throws IOException {
        ByteArrayInputStream baips = new ByteArrayInputStream(writeValueAsBytes(value, valueType));
        return WorkbookFactory.create(baips);
    }

    /*
    /**********************************************************
    /* Public API: constructing SpreadsheetWriters
    /* for more advanced configuration
    /**********************************************************
     */

    @Override
    public HyperWriter writer(final FormatSchema schema) {
        return (HyperWriter) super.writer(schema);
    }

    /**
     * Convenience method, equivalent in function to:
     * <pre>{@code
     * writer(sheetSchemaFor(type));
     * }</pre>
     */
    public HyperWriter sheetWriterFor(final Class<?> type) throws JsonMappingException {
        return writer(sheetSchemaFor(type));
    }

    /*
    /**********************************************************
    /* Public API: constructing SpreadsheetReaders
    /* for more advanced configuration
    /**********************************************************
     */

    @Override
    public HyperReader reader(final FormatSchema schema) {
        return (HyperReader) super.reader(schema);
    }

    /**
     * Convenience method, equivalent in function to:
     * <pre>{@code
     * reader(sheetSchemaFor(type)).forType(type);
     * }</pre>
     */
    public HyperReader sheetReaderFor(final Class<?> type) throws JsonMappingException {
        return reader(sheetSchemaFor(type)).forType(type);
    }

    /*
    /**********************************************************
    /* Public API: SpreadsheetSchema generation
    /**********************************************************
     */

    public HyperSchema sheetSchemaFor(final Class<?> type) throws JsonMappingException {
        return _schemaGenerator.generate(constructType(type), _serializerProvider(_serializationConfig),
            _serializerFactory);
    }

    /*
    /**********************************************************
    /* Internal methods, other
    /**********************************************************
     */

    private void _verifyValueType(final Object value) {
        // Type can NOT be a Collection or array type
        final JavaType type = constructType(value.getClass());
        if (type.isArrayType() || type.isCollectionLikeType()) {
            throw new IllegalArgumentException(
                "`valueType` MUST be specified to write a value of a Collection or array type");
        }
    }

    public static final class Builder extends MapperBuilder<HyperMapper, Builder> {

        private Builder(final HyperMapper mapper) {
            super(mapper);
        }

        public Builder enable(final BookParser.Feature... features) {
            _mapper.enable(features);
            return _this();
        }

        public Builder disable(final BookParser.Feature... features) {
            _mapper.disable(features);
            return _this();
        }

        public Builder configure(final BookParser.Feature feature, final boolean state) {
            _mapper.configure(feature, state);
            return _this();
        }

        public Builder schemaGenerator(final SchemaGenerator generator) {
            _mapper.setSchemaGenerator(generator);
            return _this();
        }

        public Builder origin(final int row, final int column) {
            return origin(new CellAddress(row, column));
        }

        public Builder origin(final String address) {
            return origin(new CellAddress(address));
        }

        public Builder origin(final CellAddress address) {
            _mapper.setOrigin(address);
            return _this();
        }

        public Builder columnNameResolver(final ColumnNameResolver resolver) {
            _mapper.setColumnNameResolver(resolver);
            return _this();
        }

        public Builder tableNameResolver(final TableNameResolver resolver) {
            _mapper.setTableNameResolver(resolver);
            return _this();
        }
    }
}
