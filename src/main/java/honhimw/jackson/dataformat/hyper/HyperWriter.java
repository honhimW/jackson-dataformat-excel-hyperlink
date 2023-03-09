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
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationConfig;
import honhimw.jackson.dataformat.hyper.ser.SheetGenerator;
import honhimw.jackson.dataformat.hyper.ser.SheetOutput;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.IOException;

public final class HyperWriter extends ObjectWriter {

    HyperWriter(final SpreadsheetMapper mapper, final SerializationConfig config,
                      final JavaType rootType, final PrettyPrinter pp) {
        super(mapper, config, rootType, pp);
    }

    HyperWriter(final SpreadsheetMapper mapper, final SerializationConfig config) {
        super(mapper, config);
    }

    HyperWriter(final SpreadsheetMapper mapper, final SerializationConfig config, final FormatSchema s) {
        super(mapper, config, s);
    }

    private HyperWriter(final HyperWriter base, final SerializationConfig config,
                              final GeneratorSettings genSettings, final Prefetch prefetch) {
        super(base, config, genSettings, prefetch);
    }

    private HyperWriter(final HyperWriter base, final SerializationConfig config) {
        super(base, config);
    }

    private HyperWriter(final HyperWriter base, final HyperFactory f) {
        super(base, f);
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    /*
    /**********************************************************************
    /* Internal factory methods, for convenience
    /**********************************************************************
     */

    @Override
    protected ObjectWriter _new(final ObjectWriter base, final JsonFactory f) {
        return new HyperWriter((HyperWriter) base, (HyperFactory) f);
    }

    @Override
    protected ObjectWriter _new(final ObjectWriter base, final SerializationConfig config) {
        return new HyperWriter((HyperWriter) base, config);
    }

    @Override
    protected ObjectWriter _new(final GeneratorSettings genSettings, final Prefetch prefetch) {
        return new HyperWriter(this, _config, genSettings, prefetch);
    }

    /*
    /**********************************************************
    /* Factory methods for creating SheetGenerators
    /**********************************************************
     */

    public SheetGenerator createGenerator(final Sheet out) {
        _assertNotNull("out", out);
        return (SheetGenerator) _configureGenerator(generatorFactory().createGenerator(out));
    }

    public SheetGenerator createGenerator(final SheetOutput<?> out) throws IOException {
        _assertNotNull("out", out);
        return (SheetGenerator) _configureGenerator(generatorFactory().createGenerator(out));
    }

    /*
    /**********************************************************
    /* Factory methods for sequence writers
    /**********************************************************
     */

    public SequenceWriter writeValues(final Sheet out) throws IOException {
        return _newSequenceWriter(false, createGenerator(out), true);
    }

    public SequenceWriter writeValues(final SheetOutput<?> out) throws IOException {
        return _newSequenceWriter(false, createGenerator(out), true);
    }

    public SequenceWriter writeValuesAsArray(final Sheet out) throws IOException {
        return _newSequenceWriter(true, createGenerator(out), true);
    }

    public SequenceWriter writeValuesAsArray(final SheetOutput<?> out) throws IOException {
        return _newSequenceWriter(true, createGenerator(out), true);
    }

    /*
    /**********************************************************
    /* Serialization methods, others
    /**********************************************************
     */

    public void writeValue(final Sheet out, final Object value) throws IOException {
        _writeValueAndClose(createGenerator(out), value);
    }

    public void writeValue(final SheetOutput<?> out, final Object value) throws IOException {
        _writeValueAndClose(createGenerator(out), value);
    }

    /*
    /**********************************************************
    /* Other public methods
    /**********************************************************
     */

    public HyperFactory generatorFactory() {
        return (HyperFactory) _generatorFactory;
    }
}
