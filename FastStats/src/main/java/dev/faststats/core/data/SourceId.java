package dev.faststats.core.data;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * An annotation to mark a source id.
 *
 * @since 0.16.0
 */
//@NonNls
//@Pattern(SourceId.PATTERN)
@Retention(RetentionPolicy.CLASS)
@Target({METHOD, FIELD, PARAMETER, LOCAL_VARIABLE})
public @interface SourceId {
    String PATTERN = "[a-z_]+";
}
