package tk.jasonho.tally.api.models.helpers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MapsTo {
    /**
     * Valid mappings
     * @return A array of valid mappings this item can map to, and map from.
     */
    String[] value() default {};
}
