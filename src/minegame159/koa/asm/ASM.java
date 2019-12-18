package minegame159.koa.asm;

public class ASM {
    public static final String OBJECT = "java/lang/Object";
    public static final String OBJECT_D = "Ljava/lang/Object;";

    public static final String RUNNABLE = "java/lang/Runnable";
    public static final String RUNNABLE_D = "Ljava/lang/Runnable;";

    public static final String SYSTEM = "java/lang/System";
    public static final String SYSTEM_D = "Ljava/lang/System;";

    public static final String STRING = "java/lang/String";
    public static final String STRING_D = "Ljava/lang/String;";

    public static final String PRINTSTREAM = "java/io/PrintStream";
    public static final String PRINTSTREAM_D = "Ljava/io/PrintStream;";

    public static final String GLOBALS = "minegame159/koa/Globals";
    public static final String GLOBALS_D = "Lminegame159/koa/Globals;";

    public static final String VALUE = "minegame159/koa/Value";
    public static final String VALUE_D = "Lminegame159/koa/Value;";

    public static final String VALUETYPE = "minegame159/koa/Value$Type";
    public static final String VALUETYPE_D = "Lminegame159/koa/Value$Type;";

    public static final String VALUE_NULL = "minegame159/koa/Value$Null";
    public static final String VALUE_NULL_D = "Lminegame159/koa/Value$Null;";

    public static final String VALUE_BOOL = "minegame159/koa/Value$Bool";
    public static final String VALUE_BOOL_D = "Lminegame159/koa/Value$Bool;";

    public static final String VALUE_NUMBER = "minegame159/koa/Value$Number";
    public static final String VALUE_NUMBER_D = "Lminegame159/koa/Value$Number;";

    public static final String VALUE_TABLE = "minegame159/koa/Value$Table";
    public static final String VALUE_TABLE_D = "Lminegame159/koa/Value$Table;";

    public static final String VALUE_FUNCTION = "minegame159/koa/Value$Function";
    public static final String VALUE_FUNCTION_D = "Lminegame159/koa/Value$Function;";

    public static final String WRONGTYPEEXCEPTION = "minegame159/koa/WrongTypeException";
    public static final String WRONGTYPEEXCEPTION_D = "Lminegame159/koa/WrongTypeException;";

    public static final String WRONGNUMBEROFARGUENTSEXCEPTION = "minegame159/koa/WrongNumberOfArgumentsException";
    public static final String WRONGNUMBEROFARGUENTSEXCEPTION_D = "Lminegame159/koa/WrongNumberOfArgumentsException;";

    public static final String STRING_TABLE = "minegame159/koa/tables/StringTable";
    public static final String STRING_TABLE_D = "Lminegame159/koa/tables/StringTable;";

    public static String methodDescriptor(String... descriptors) {
        String descriptor = "(";
        for (int i = 0; i < descriptors.length; i++) {
            if (i == descriptors.length - 1) descriptor += ")";
            descriptor += descriptors[i];
        }
        return descriptor;
    }
}
