package es.icarto.gvsig.elle.style;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;

public class ReplaceFieldLabelTest {

    @Test
    @Ignore
    public void learning() {
        Pattern p = Pattern.compile("\\[(cat)\\]");
        Matcher m = p.matcher("one [cat] two [cats] in the cat yard");
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            System.out.println(m.group(0));
            System.out.println(m.group(1));
            m.appendReplacement(sb, "dog");
        }
        m.appendTail(sb);
        System.out.println(sb.toString());
    }

    @Test
    public void testReplacement() {
        FieldLabelRE replace = new FieldLabelRE();

        assertEquals("[competenci]\" (pk \"[quelargoes]\")\"",
                replace.limitTo10("[competencia]\" (pk \"[quelargoestecampo]\")\""));

        assertEquals("[denominaci]", replace.limitTo10("[denominacion]"));

        assertEquals("[foo]", replace.limitTo10("[foo]"));

        assertEquals("[competenci]\" (pk \"[bar]\")\"", replace.limitTo10("[competencia]\" (pk \"[bar]\")\""));

        assertEquals("\"Lecho frenado\"", replace.limitTo10("\"Lecho frenado\""));

    }

    @Test
    /**
     * Chequea que la expresión regular detecta adecuadamente los campos de la capa dentro de expresiones del
     * etiqueta avanzado
     *
     */
    public void testFind() {

        FieldLabelRE re = new FieldLabelRE();

        Matcher matcher = null;
        String labelExp = null;

        labelExp = "[denominacion]";
        matcher = re.matcher(labelExp);
        assertTrue(matcher.matches());
        assertEquals("denominacion", matcher.group(1));

        labelExp = "\"Lecho frenado\"";
        matcher = re.matcher(labelExp);
        assertFalse(matcher.matches());

        labelExp = "[nombre]\" (pk \"[pk]\")\"";
        matcher = re.matcher(labelExp);

        assertTrue(matcher.find());
        assertEquals("nombre", matcher.group(1));
        assertTrue(matcher.find());
        assertEquals("pk", matcher.group(1));

        labelExp = "[nombre]\" (pk \"[pk]\")\"";
        matcher = re.matcher(labelExp);

        assertTrue(matcher.find());
        assertEquals("nombre", matcher.group(1));
        assertTrue(matcher.find());
        assertEquals("pk", matcher.group(1));

        // Fallará cuando haya algo como [foo] entre comillas como si fuera un texto estático

    }

}
