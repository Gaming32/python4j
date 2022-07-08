package io.github.gaming32.python4j.bytecode;

import java.io.ByteArrayOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import io.github.gaming32.python4j.bytecode.Disassemble.ExceptionTableEntry;
import io.github.gaming32.python4j.bytecode.Disassemble.Instruction;
import io.github.gaming32.python4j.objects.PyCodeObject;

public class Bytecode implements Iterable<Instruction> {
    private final PyCodeObject codeObj;
    private final int firstLine;
    private final Map<Integer, Integer> lineStarts;
    private final OptionalInt currentOffset;
    private final List<ExceptionTableEntry> exceptionEntries;
    private final boolean showCaches;

    public Bytecode(PyCodeObject co) {
        this(co, OptionalInt.empty(), false);
    }

    public Bytecode(PyCodeObject co, boolean showCaches) {
        this(co, OptionalInt.empty(), showCaches);
    }

    public Bytecode(PyCodeObject co, OptionalInt currentOffset) {
        this(co, currentOffset, false);
    }

    public Bytecode(PyCodeObject co, OptionalInt currentOffset, boolean showCaches) {
        codeObj = co;
        firstLine = codeObj.getCo_firstlineno();
        lineStarts = Map.copyOf(Disassemble.findLineStarts(co));
        this.currentOffset = currentOffset;
        exceptionEntries = List.copyOf(Disassemble.parseExceptionTable(co));
        this.showCaches = showCaches;
    }

    public PyCodeObject getCodeObj() {
        return codeObj;
    }

    public int getFirstLine() {
        return firstLine;
    }

    public Map<Integer, Integer> getLineStarts() {
        return lineStarts;
    }

    public OptionalInt getCurrentOffset() {
        return currentOffset;
    }

    public List<ExceptionTableEntry> getExceptionEntries() {
        return exceptionEntries;
    }

    public boolean isShowCaches() {
        return showCaches;
    }

    public List<Instruction> getInstructions() {
        return Disassemble.getInstructionsBytes(
            codeObj.getCo_code().toByteArray(),
            codeObj::varnameFromOparg,
            codeObj.getCo_names(), codeObj.getCo_consts(),
            lineStarts, exceptionEntries,
            showCaches
        );
    }

    @Override
    public Iterator<Instruction> iterator() {
        return getInstructions().iterator();
    }

    public String info() {
        return Disassemble.codeInfo(codeObj);
    }

    public String dis() {
        try (
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(baos, true);
        ) {
            Disassemble.disassembleBytes0(
                codeObj.getCo_code().toByteArray(), out,
                codeObj::varnameFromOparg,
                codeObj.getCo_names(), codeObj.getCo_consts(),
                lineStarts, exceptionEntries,
                showCaches
            );
            return baos.toString(); // PrintStream uses the default charset, so we're just encoding and decoding with the same charset
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    @Override
    public String toString() {
        return "Bytecode[" + codeObj + "]";
    }
}
