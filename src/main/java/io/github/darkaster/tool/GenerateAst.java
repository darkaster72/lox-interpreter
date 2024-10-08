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
                "Assign   : Token name, Expr value",
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Logical  : Expr left, Token operator, Expr right",
                "Set      : Expr object, Token name, Expr value",
                "This     : Token keyword",
                "Super    : Token keyword, Token method",
                "Get      : Expr object, Token name",
                "Unary    : Token operator, Expr right",
                "Call     : Expr callee, Token paren, List<Expr> arguments",
                "Variable : Token name"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Block      : List<Stmt> statements",
                "Expression : Expr expression",
                "Function   : Token name, List<Token> param, List<Stmt> body",
                "Getter     : Token name, List<Stmt> body",
                "Class      : Token name, Expr.Variable superclass, List<Stmt.Function> functions",
                "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
                "Print      : Expr expression",
                "Var        : Token name, Expr initializer",
                "While      : Expr condition, Stmt statement",
                "Return     : Token keyword, Expr value"
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

        defineVisitor(writer, baseName, types);

        // The base accept() method.
        writer.println("\t\t\tabstract <R> R accept(Visitor<R> visitor);");
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

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("\t\tinterface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.printf("\t\t\tR visit%s%s(%s %s);%n", typeName, baseName, typeName, baseName.toLowerCase());
        }

        writer.println("\t\t}");
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

        // Visitor pattern.
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.printf("      return visitor.visit%s%s(this);%n", className, baseName);
        writer.println("    }");

        writer.println("\t}");
    }
}
