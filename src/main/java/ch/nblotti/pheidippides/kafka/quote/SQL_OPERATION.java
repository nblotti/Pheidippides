package ch.nblotti.pheidippides.kafka.quote;

public enum SQL_OPERATION {

    EMPTY("e"),
    CREATE("c"),
    ERROR("x"),
    READ("r"),
    UPDATE("u"),
    DELETE("d");


    private final String operationStr;


    SQL_OPERATION(String operationStr) {
        this.operationStr = operationStr;
    }

    public static SQL_OPERATION fromString(String operationStr) {
        for (SQL_OPERATION operation : SQL_OPERATION.values())
            if (operation.operationStr.equals(operationStr))
                return operation;

        throw new IllegalStateException("wrong operation code");
    }


}
