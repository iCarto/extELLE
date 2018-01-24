package es.icarto.gvsig.elle.style;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldLabelRE {

    private final Pattern p = Pattern.compile("\\[(\\w+)\\]");

    public String limitTo10(String s) {
        Matcher m = p.matcher(s);

        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String replacement = m.group(1);
            int l = Math.min(replacement.length(), 10);
            replacement = "[" + replacement.substring(0, l) + "]";
            m.appendReplacement(sb, replacement);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    Matcher matcher(String s) {
        return p.matcher(s);
    }

}
