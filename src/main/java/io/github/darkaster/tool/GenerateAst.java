package io.github.darkaster.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token operator, Expr right"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package io.github.darkaster.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        // The AST classes.
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
            writer.println();
        }

        writer.println("}");
        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println(String.format("\tstatic class %s extends %s {", className, baseName));
        String[] fields = fieldList.split(", ");

        // Fields.
        for (String field : fields) {
            writer.println("\t\tfinal " + field + ";");
        }
        writer.println();

        // Constructor
        writer.println(String.format("\t\tpublic %s(%s) {", className, fieldList));

        /// Store parameters in fields.

        for (String field : fields) {
            String name = field.split(" ")[1];

            writer.println(String.format("\t\t\tthis.%s = %s;", name, name));
        }
        writer.println("\t\t}");

        writer.println("\t}");
    }
}
