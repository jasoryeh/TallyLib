package tk.jasonho.tally.core.bukkit.pseudo;

import com.google.common.base.Preconditions;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class LocaleStrings {
    private final Locale locale;
    private final Map<String, String> strings;

    public LocaleStrings(Locale locale) {
        this(locale, new HashMap());
    }

    public LocaleStrings(Locale locale, Map<String, String> strings) {
        this.locale = locale;
        this.strings = strings;
    }

    public static LocaleStrings fromXml(InputStream stream) throws JDOMException, IOException {
        SAXBuilder sax = new SAXBuilder();
        Document doc = sax.build(stream);
        return fromXml(doc.getRootElement());
    }

    public static LocaleStrings fromXml(Element el) {
        Preconditions.checkArgument(el.getName().equals("locale"), "element not <lang/>");
        Preconditions.checkArgument(el.getAttribute("lang") != null, "<lang/> missing lang attribute");
        String lang = el.getAttributeValue("lang");
        String country = el.getAttributeValue("country");
        Locale locale = new Locale(lang);
        if (country != null) {
            locale = new Locale(lang, country);
        }

        LocaleStrings strings = new LocaleStrings(locale);
        Iterator var5 = el.getDescendants().iterator();

        while(var5.hasNext()) {
            Content content = (Content)var5.next();
            if (content instanceof Element) {
                Element child = (Element)content;
                if (child.getChildren().size() <= 0) {
                    String path = getPath(el, child);
                    strings.add(path, Strings.addColors(child.getTextTrim().replaceAll(" +", " ")));
                }
            }
        }

        return strings;
    }

    private static String getPath(Element exclude, Element nested) {
        String result = nested.getName();

        for(Element curr = nested.getParentElement(); !curr.equals(exclude); curr = curr.getParentElement()) {
            result = curr.getName() + "." + result;
        }

        return result;
    }

    public void add(String key, String value) {
        this.strings.put(key, value);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(this.strings.get(key));
    }

    public String toString() {
        return "LocaleStrings(locale=" + this.getLocale() + ", strings=" + this.strings + ")";
    }

    public Locale getLocale() {
        return this.locale;
    }
}
