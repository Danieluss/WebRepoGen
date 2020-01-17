package org.webrepogen.annotations;

import java.io.PrintWriter;
import java.io.Writer;

public class ProcessingWriter extends PrintWriter {

    public ProcessingWriter(Writer out) {
        super(out);
    }

    private String getName(String type) {
        return type.substring(type.lastIndexOf('.') + 1);
    }

    public String importType(String type) {
        println("import " + type + ";");
        return getName(type);
    }

    public void packageName(String name) {
        println("package " + name + ";");
    }

}
