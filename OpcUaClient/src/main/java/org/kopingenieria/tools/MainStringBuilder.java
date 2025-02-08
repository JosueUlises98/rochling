package org.kopingenieria.tools;


public class MainStringBuilder {
    public static void main(String[] args) {
        StringBuilderClass stringBuilderClass = new StringBuilderClass(80);
        String string = stringBuilderClass.append('c')
                .append('l')
                .append('a')
                .append('s')
                .append('e')
                .append("StringBuilderAPI")
                .append(2)
                .append(2)
                .build();
        //Impresion del error o excepcion
        System.out.println(string);
    }
}
